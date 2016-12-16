var authToken = localStorage.getItem("authToken"); 
if(authToken!=null && authToken!='null' && authToken!=''){
	//test to make sure is still valid
	var parameters = {};
	parameters['AuthToken'] = authToken;
	$.ajax({
        type: 'POST',
        url: '/api/loginTest',
        data: JSON.stringify(parameters),
        dataType: 'json',
        contentType: 'application/json',
        success: function(data){
        	console.log('Success login test');
//        	console.log(data.toString());
        	//All ok
		},
        failure: function(data){
        	console.log('Failure login test');
            console.log(data.toString());
            localStorage.setItem("authToken", null);
        	window.location.replace('/goss/login.html?refer='+window.location.pathname);
        },
        error: function(data){
        	console.log('Error login test');
            console.log(data.toString());
            
            localStorage.setItem("authToken", null);
        	window.location.replace('/goss/login.html?refer='+window.location.pathname);

        }

    });	

} else {
	window.location.replace('/goss/login.html?refer='+window.location.pathname);
}
