var authToken = localStorage.getItem("authToken"); 
if(authToken==null || authToken=='null' || authToken==''){ 
	window.location.replace('/goss/login.html?refer='+window.location.pathname);
		
}
