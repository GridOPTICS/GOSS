<html>
<body>
	<h2>DSA Management</h2>
	
	<form action="changeOption" method="post">
	<table>
		<caption>DSA Display Time</caption>
		<tr>
			<td>Option:</td>
			<td>
				<input type="radio" name="timeOption" id="currentTime" value="checked" /> Current Time<br/>
				<input type="radio" name="timeOption" id="currentTimeOffset" /> Current Time Offset: <input type="text" name="offset" id="offset" /><br />
			</td>
		</tr>
		<tr>
			<td colspan="2"><input type="submit" value="Update" /></td>
		</tr>
	</table>
	</form>
</body>
</html>
