"""
STOMP Token Authentication Integration Test

Tests the full token authentication flow for an external STOMP client
connecting to a GOSS server:

  1. Connect via STOMP with username/password
  2. Request a JWT token from the token topic
  3. Verify the token is returned and non-empty
  4. Disconnect and reconnect using the token as credentials
  5. Verify the token-based connection can publish/subscribe

Requires a running GOSS server with token authentication enabled.
Configure via environment variables or command-line arguments.

Usage:
    # Install dependencies (from pnnl.goss.core.itests/)
    pixi install

    # Run via pixi
    pixi run test-stomp-token

    # Against default localhost:61613 with system/manager
    pixi run test-stomp-token-standalone

    # Against a specific host
    pixi run python src/pnnl/goss/core/itests/test_stomp_token_auth.py \\
        --host 192.168.1.10 --port 61613

    # With custom credentials
    pixi run python src/pnnl/goss/core/itests/test_stomp_token_auth.py \\
        --username myuser --password mypass

    # Run with pytest directly
    pixi run pytest src/pnnl/goss/core/itests/test_stomp_token_auth.py -v
"""

import argparse
import base64
import logging
import os
import sys
import threading
import time
import uuid

import stomp

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)-8s [%(name)s] %(message)s",
)
log = logging.getLogger("test_stomp_token_auth")

# ---------------------------------------------------------------------------
# Configuration defaults (overridable via env vars or CLI args)
# ---------------------------------------------------------------------------
STOMP_HOST = os.environ.get("GOSS_STOMP_HOST", "localhost")
STOMP_PORT = int(os.environ.get("GOSS_STOMP_PORT", "61618"))
USERNAME = os.environ.get("GOSS_USERNAME", "system")
PASSWORD = os.environ.get("GOSS_PASSWORD", "manager")
TOKEN_TOPIC = "/topic/pnnl.goss.token.topic"
TOKEN_TIMEOUT_S = 10
HEARTBEAT_MS = 10000


# ---------------------------------------------------------------------------
# Listener helpers
# ---------------------------------------------------------------------------
class TokenResponseListener(stomp.ConnectionListener):
    """Listens on a temporary queue for the JWT token response."""

    def __init__(self):
        self.token = None
        self.error = None
        self._event = threading.Event()

    def on_message(self, frame):
        body = frame.body if hasattr(frame, "body") else str(frame)
        log.info("Token response received (%d bytes)", len(body))
        self.token = body
        self._event.set()

    def on_error(self, frame):
        body = frame.body if hasattr(frame, "body") else str(frame)
        log.error("STOMP error during token request: %s", body)
        self.error = body
        self._event.set()

    def wait(self, timeout=TOKEN_TIMEOUT_S):
        return self._event.wait(timeout)


class PubSubListener(stomp.ConnectionListener):
    """Listens for a single message on a subscribed topic."""

    def __init__(self):
        self.received_message = None
        self.received_headers = None
        self.error = None
        self._event = threading.Event()

    def on_message(self, frame):
        self.received_headers = frame.headers if hasattr(frame, "headers") else {}
        self.received_message = frame.body if hasattr(frame, "body") else str(frame)
        log.debug("PubSub received: %s", self.received_message[:200])
        self._event.set()

    def on_error(self, frame):
        body = frame.body if hasattr(frame, "body") else str(frame)
        log.error("STOMP error in pub/sub: %s", body)
        self.error = body
        self._event.set()

    def wait(self, timeout=5):
        return self._event.wait(timeout)


# ---------------------------------------------------------------------------
# Test helpers
# ---------------------------------------------------------------------------
def create_connection(host, port):
    """Create a new STOMP 1.2 connection (not yet connected)."""
    return stomp.Connection12(
        [(host, port)],
        heartbeats=(HEARTBEAT_MS, HEARTBEAT_MS),
    )


def request_token(host, port, username, password):
    """
    Connect with credentials, request a JWT token, and return it.

    The GOSS token flow:
      - Connect via STOMP with username/password
      - Subscribe to a temporary reply queue
      - Send base64(username:password) to the token topic with reply-to header
      - Wait for the server to respond with a JWT token
    """
    conn = create_connection(host, port)
    listener = TokenResponseListener()
    conn.set_listener("token_listener", listener)

    log.info("Connecting to %s:%d as '%s' to request token...", host, port, username)
    conn.connect(username, password, wait=True)
    assert conn.is_connected(), "Failed to connect with username/password"
    log.info("Connected successfully with credentials")

    # Use uuid to avoid reply-queue collisions in rapid test runs
    reply_dest = f"temp.token_resp.{username}-{uuid.uuid4().hex[:12]}"
    auth_payload = base64.b64encode(f"{username}:{password}".encode()).decode()

    # Subscribe to the temp queue where the token response will arrive
    conn.subscribe(destination=f"/queue/{reply_dest}", id="token-sub-1", ack="auto")
    log.info("Subscribed to /queue/%s", reply_dest)

    # Allow subscription to propagate to broker before sending
    time.sleep(0.3)

    # Send the token request
    conn.send(
        destination=TOKEN_TOPIC,
        body=auth_payload,
        headers={"reply-to": f"/queue/{reply_dest}"},
    )
    log.info("Sent token request to %s", TOKEN_TOPIC)

    # Wait for the token
    got_response = listener.wait(TOKEN_TIMEOUT_S)
    token = listener.token

    # Disconnect the credential-based connection
    try:
        conn.disconnect()
    except Exception as e:
        log.debug("Non-critical error during disconnect: %s", e)

    return got_response, token, listener.error


def connect_with_token(host, port, token):
    """
    Connect using a JWT token as the username with an empty password.
    Returns the connected stomp.Connection12.
    """
    conn = create_connection(host, port)
    log.info("Connecting with token (%d bytes)...", len(token))
    try:
        conn.connect(token, "", wait=True)
    except stomp.exception.ConnectFailedException as e:
        raise AssertionError(
            f"Token-based STOMP connect failed. Token length={len(token)}, "
            f"token prefix={token[:40]}... Error: {e}"
        ) from e
    return conn


def verify_pubsub_with_token(conn, token):
    """
    Verify the token-based connection can publish and subscribe.
    Sends a test message on a topic and verifies receipt.
    """
    test_topic = "/topic/goss.test.stomp.token.auth"
    test_body = f'{{"test": "token_auth", "timestamp": {time.time()}}}'

    listener = PubSubListener()
    conn.set_listener("pubsub_listener", listener)
    conn.subscribe(destination=test_topic, id="pubsub-sub-1", ack="auto")
    log.info("Subscribed to %s", test_topic)

    # Allow subscription to propagate to broker
    time.sleep(0.5)

    conn.send(
        destination=test_topic,
        body=test_body,
        headers={"GOSS_HAS_SUBJECT": "true", "GOSS_SUBJECT": token},
    )
    log.info("Published test message to %s", test_topic)

    got_message = listener.wait(5)
    return got_message, listener.received_message


# ---------------------------------------------------------------------------
# Test cases (usable with pytest or standalone)
# ---------------------------------------------------------------------------
class TestStompTokenAuth:
    """Integration tests for STOMP token authentication against a live GOSS server."""

    host = STOMP_HOST
    port = STOMP_PORT
    username = USERNAME
    password = PASSWORD

    def test_01_credential_connect(self):
        """Verify basic STOMP connection with username/password works."""
        conn = create_connection(self.host, self.port)
        conn.connect(self.username, self.password, wait=True)
        assert conn.is_connected(), "Should connect with valid credentials"
        log.info("PASS: credential connect")
        conn.disconnect()

    def test_02_token_request_returns_nonempty_token(self):
        """Request a token and verify it is returned non-empty (the core bug fix)."""
        got_response, token, error = request_token(
            self.host, self.port, self.username, self.password
        )
        assert got_response, f"Should get a token response within {TOKEN_TIMEOUT_S}s"
        assert error is None, f"Should not get an error: {error}"
        assert token is not None, "Token must not be None"
        assert len(token.strip()) > 0, "Token must not be empty"
        assert token != "authentication failed", "Token request should not fail auth"
        # JWT tokens have 3 dot-separated parts (header.payload.signature)
        parts = token.split(".")
        assert len(parts) == 3, (
            f"Token should be a JWT with 3 parts (header.payload.signature), "
            f"got {len(parts)} parts: {token[:80]}..."
        )
        log.info("PASS: received valid JWT token (%d bytes, 3 parts)", len(token))
        # Stash for dependent tests
        TestStompTokenAuth._token = token

    def test_03_connect_with_token(self):
        """Reconnect using the JWT token as credentials."""
        token = getattr(TestStompTokenAuth, "_token", None)
        assert token is not None, (
            "test_03 depends on test_02 having produced a valid token. "
            "Run tests sequentially: pytest -v (tests are ordered by name)."
        )

        conn = connect_with_token(self.host, self.port, token)
        assert conn.is_connected(), "Should connect with token"
        log.info("PASS: connected with token")
        conn.disconnect()

    def test_04_pubsub_with_token(self):
        """Verify publish/subscribe works on a token-authenticated connection."""
        token = getattr(TestStompTokenAuth, "_token", None)
        assert token is not None, (
            "test_04 depends on test_02 having produced a valid token. "
            "Run tests sequentially: pytest -v (tests are ordered by name)."
        )

        conn = connect_with_token(self.host, self.port, token)
        assert conn.is_connected(), "Should connect with token"

        got_message, received = verify_pubsub_with_token(conn, token)
        assert got_message, "Should receive the published message"
        assert received is not None, "Received message should not be None"
        assert "token_auth" in received, f"Message content mismatch: {received}"
        log.info("PASS: pub/sub works with token auth")
        conn.disconnect()

    def test_05_invalid_credentials_no_token(self):
        """Verify that invalid credentials do not produce a valid token."""
        try:
            got_response, token, error = request_token(
                self.host, self.port, "baduser", "badpass"
            )
        except (stomp.exception.ConnectFailedException, AssertionError):
            # Connection refused with bad credentials -- expected
            log.info("PASS: invalid credentials rejected at STOMP connect")
            return

        # If connection succeeded, verify no valid JWT was issued
        if got_response and token:
            assert token == "authentication failed" or len(token.split(".")) != 3, (
                f"Invalid credentials should not produce a valid JWT, got: {token[:80]}"
            )
            log.info("PASS: server returned auth failure message")
        else:
            # No response within timeout -- also acceptable (server ignored bad creds)
            log.info("PASS: no token response for invalid credentials (timeout)")

    def test_06_empty_token_rejected(self):
        """Verify that connecting with an empty string as token fails."""
        conn = create_connection(self.host, self.port)
        connected = False
        try:
            conn.connect("", "", wait=True, headers={"accept-version": "1.2"})
            connected = conn.is_connected()
        except (stomp.exception.ConnectFailedException, Exception):
            log.info("PASS: empty token connection correctly refused")
            return
        finally:
            try:
                conn.disconnect()
            except Exception:
                pass

        if connected:
            # Broker allows anonymous -- this test is not meaningful in
            # that configuration, so skip rather than false-pass.
            try:
                import pytest
                pytest.skip(
                    "Broker allows anonymous connections; "
                    "empty-token rejection cannot be verified."
                )
            except ImportError:
                log.warning(
                    "SKIP: broker allows anonymous connections; "
                    "empty-token rejection cannot be verified."
                )


# ---------------------------------------------------------------------------
# Standalone runner
# ---------------------------------------------------------------------------
def run_all_tests(host, port, username, password):
    """Run all tests sequentially, reporting pass/fail."""
    tests = TestStompTokenAuth()
    tests.host = host
    tests.port = port
    tests.username = username
    tests.password = password

    test_methods = [m for m in sorted(dir(tests)) if m.startswith("test_")]
    passed = 0
    failed = 0
    skipped = 0
    errors = []

    print(f"\n{'='*60}")
    print(f"GOSS STOMP Token Authentication Tests")
    print(f"Server: {host}:{port}  User: {username}")
    print(f"{'='*60}\n")

    for method_name in test_methods:
        method = getattr(tests, method_name)
        doc = method.__doc__ or method_name
        print(f"  {method_name}: {doc.strip()}")
        try:
            method()
            passed += 1
            print(f"    -> PASSED\n")
        except Exception as e:
            if "SKIP" in str(e) or "skip" in type(e).__name__.lower():
                skipped += 1
                print(f"    -> SKIPPED: {e}\n")
            else:
                failed += 1
                errors.append((method_name, e))
                print(f"    -> FAILED: {e}\n")

    print(f"{'='*60}")
    print(f"Results: {passed} passed, {failed} failed, {skipped} skipped "
          f"out of {passed + failed + skipped}")
    print(f"{'='*60}")

    if errors:
        print("\nFailures:")
        for name, err in errors:
            print(f"  {name}: {err}")
        return 1
    return 0


def main():
    parser = argparse.ArgumentParser(
        description="Test STOMP token authentication against a GOSS server"
    )
    parser.add_argument(
        "--host", default=STOMP_HOST, help=f"STOMP host (default: {STOMP_HOST})"
    )
    parser.add_argument(
        "--port", type=int, default=STOMP_PORT,
        help=f"STOMP port (default: {STOMP_PORT})"
    )
    parser.add_argument(
        "--username", default=USERNAME, help=f"Username (default: {USERNAME})"
    )
    parser.add_argument(
        "--password", default=PASSWORD, help=f"Password (default: {PASSWORD})"
    )
    args = parser.parse_args()

    rc = run_all_tests(args.host, args.port, args.username, args.password)
    sys.exit(rc)


if __name__ == "__main__":
    main()
