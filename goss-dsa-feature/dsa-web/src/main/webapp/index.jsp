<html>
<body>
	<h2>DSA Management</h2>
	
	<form action="dsa-service/update" method="post">
	<table>
		<caption>DSA Display Time</caption>
		<tr>
			<td>Option:</td>
			<td>
				<input type="radio" name="timeOption" id="currentTime" value="currentTime" checked="checked" /> Current Time<br/>
				<input type="radio" name="timeOption" id="currentTimeOffset" value="currentTimeOffset" /> Current Time Offset: <input type="text" name="offset" id="offset" /> (+/- hh:mm ex 08:00 for +8 hours)<br />
			</td>
		</tr>
		<tr>
			<td colspan="2"><input type="submit" value="Update" /></td>
		</tr>
	</table>
	</form>
</body>
</html>
