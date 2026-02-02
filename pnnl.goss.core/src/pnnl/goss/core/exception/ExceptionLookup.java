package pnnl.goss.core.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;

import com.northconcepts.exception.ConnectionCode;
import com.northconcepts.exception.ErrorCode;
import com.northconcepts.exception.ErrorText;

@Component(service = ErrorText.class)
public class ExceptionLookup implements ErrorText {

    private Map<String, String> lookupMap;

    private void initialize() {
        if (lookupMap != null)
            return;

        lookupMap = new HashMap<>();

        lookupMap.put(getKey(ConnectionCode.class, ConnectionCode.SESSION_ERROR),
                "Could not create a valid session");

    }

    @Activate
    public void start() {
        initialize();
    }

    @Deactivate
    public void stop() {
        lookupMap.clear();
        lookupMap = null;
    }

    private String getKey(Class<? extends ErrorCode> codeClass, ErrorCode code) {
        return codeClass.getSimpleName() + "__" + code;
    }

    @Override
    public String getText(ErrorCode code) {
        String key = getKey(code.getClass(), code);
        return Optional.ofNullable((String) lookupMap.get(key))
                .orElse("An unknown error code: " + code + "dedtected");
    }
}
