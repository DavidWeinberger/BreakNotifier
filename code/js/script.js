function loginClicked()
{
	httpPost();
}

function httpGet()
{
	
	
}

function httpPost()
{
	var xhr = new XMLHttpRequest();
	var url = "https://mese.webuntis.com/WebUntis/j_spring_security_check";
	xhr.open("POST", url, true);
	xhr.setRequestHeader("Content-Type", "application/json");
	xhr.onreadystatechange = function () {
	    if (xhr.readyState === 4 && xhr.status === 200) {
	        //var json = JSON.parse(xhr.responseText);
	        //document.getElementById("test").innerHTML = (xhr.responseText);
	    }
	    document.getElementById("test").innerHTML = (xhr.responseText);
	    console.log(xhr.responseText)
	};
	var data = JSON.stringify({"school":"htbla linz leonding", "j_username":document.getElementById("username").innerHTML, "j_password":document.getElementById("password"), "token":""});
	xhr.send(data);
}