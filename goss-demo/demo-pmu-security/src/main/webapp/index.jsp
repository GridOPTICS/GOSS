<html>
<body>
	<h2>Hello GOSS!</h2>

	<%
        String myname =  (String)session.getAttribute("username");
        
        if(myname!=null)
            {
             out.println("Welcome  "+myname+"  , <a href=\"logout\" >Logout</a>");
            }
        else 
            {
            %>
	<form action="checkLogin" method="POST">
		<table>
			<tr>
				<td>Username :</td>
				<td><input name="username" size=15 type="text" /></td>
			</tr>
			<tr>
				<td>Password :</td>
				<td><input name="pass" size=15 type="password" /></td>
			</tr>
		</table>
		<input type="submit" value="login" />
	</form>
	<% 
            }
         
             
            %>

</body>
</html>
