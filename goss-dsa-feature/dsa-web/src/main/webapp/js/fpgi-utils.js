/*!
*
 */
function getParameterByName(name)
{
  name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
  var regexS = "[\\?&]" + name + "=([^&#]*)";
  var regex = new RegExp(regexS);
  var results = regex.exec(window.location.search);
  if(results == null)
	return "";
  else
	return decodeURIComponent(results[1].replace(/\+/g, " "));
}


function formatDate(dateToFormat){
	return (dateToFormat.getMonth()+1)+"/"+dateToFormat.getDate()+"/"+dateToFormat.getFullYear()+" "+dateToFormat.getHours()+":"+dateToFormat.getMinutes()+":"+dateToFormat.getSeconds();
}


function getMostRecentTime(dataItems){
	var mostRecent=0;
	if(dataItems!=null && dataItems.length>0){
		var i=dataItems.length-1;
		
		while(i>0 && dataItems[i][0]>mostRecent ){
			//alert(dataItems[i][0]+'  '+dataItems[i][1]);
			if(dataItems[i][1]>0){
			//if(dataItems[dataItems.length-1][0]>mostRecent){
				mostRecent = dataItems[i][0];
				mostRecent++;		
//alert('setting most recent to pos '+i);				
			//}
			}
			i--;
		}
	}			
	return mostRecent;
}


function getTimelineOptionsFreq(color, endTime, showMS, showLines, showPoints,showBars,yLabel,decimals){
	var plotOptionsEvents = {
						colors: [ "#99CC66", "#999966","#FFCC99","#CCCC99","#996633","#999999","#669999","#663333","#CCCCCC","#6666CC","#663300"],
						lines: { show: true },
						points: { show: false },
						grid: { hoverable: true, clickable: true },
						legend: {position: "ne", margin: [-150,0]},
						xaxis: 
						{	mode:"time",
							timeformat: "%m/%d %H:%M:%S",
							min: endTime,
							max: endTime+showMS
						},
						yaxis: {
							tickFormatter: function(val, axis) { return val < axis.max ? val.toFixed(2) : "hz";}
						},
						zoom: {
							interactive: true
						},
						pan: {
							interactive: true
						},
						crosshair: { mode: "x" }			
					};		

			
				
	return plotOptionsEvents;
}
function getTimelineOptions(color, endTime, showMS, showLines, showPoints,showBars,yLabel,decimals){
	var plotOptionsEvents = {
						colors: [color, "#999966","#FFCC99","#CCCC99","#996633","#999999","#669999","#663333","#CCCCCC","#6666CC","#663300"],
						lines: { show: showLines },
						points: { show: showPoints },
						bars: { show: showBars },
						grid: { hoverable: true, clickable: true },
						legend: {position: "ne", margin: [-80,0]},
						xaxis: 
						{	mode:"time",
							timeformat: "%m/%d %H:%M:%S",
							min: endTime,
							max: endTime+showMS
						},
						yaxis: {
							tickFormatter: function(val, axis) { return val < axis.max ? val.toFixed(decimals) : yLabel;}
						},
						zoom: {
							interactive: false
						},
						pan: {
							interactive: true
						},
						crosshair: { mode: "x" }			
					};		

	return plotOptionsEvents;
}