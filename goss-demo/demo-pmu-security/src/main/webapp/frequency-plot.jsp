<%@ page import="pnnl.goss.demo.security.util.DemoConstants"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>PMU Frequency Chart</title>
<link rel="stylesheet" href="css/green/style.css" type="text/css"
	media="print, projection, screen">
<link rel="stylesheet"
	href="css/ui-darkness/jquery-ui-1.8.22.custom.css" type="text/css"
	media="print, projection, screen">

<!--[if lte IE 8]><script language="javascript" type="text/javascript" src="js/flot/excanvas.min.js"></script><![endif]-->
<script language="javascript" type="text/javascript"
	src="js/flot/jquery.js"></script>
<script language="javascript" type="text/javascript"
	src="js/flot/jquery.flot.js"></script>
<script language="javascript" type="text/javascript"
	src="js/flot/jquery.flot.navigate.js"></script>
<script language="javascript" type="text/javascript"
	src="js/flot/jquery.flot.crosshair.js"></script>
<script language="javascript" type="text/javascript"
	src="js/flot/jquery.flot.resize.js"></script>


<script type="text/javascript" src="js/jquery-ui-1.8.22.custom.min.js"></script>
<script language="javascript" type="text/javascript"
	src="js/jquery.timepicker.js"></script>
<script language="javascript" type="text/javascript"
	src="js/fpgi-utils.js"></script>

</head>
<body>
	<p align="right"><a href="logout">Logout</a></p>
	<h1>
		PMU Frequency Chart <span id="pollingImg" style="visibility: hidden"><img
			src="refresh.png" alt="Polling for new Events" /></span>
	</h1>
	
	
	<script type="text/javascript">
	
	
	$(function () {
		var doPlotFreq = false;
		
		var pmuList = '';
		
		var dpStart = getParameterByName("datepickerstart");			
		if(dpStart==null || dpStart==''){
			//var now = new Date();			
			//dpStart = (now.getMonth()+1)+"/"+now.getDate()+"/"+now.getFullYear()+" 00:00:00";
			dpStart = '<%=DemoConstants.getDefaultStart()%>'
		}
		$('#datepickerstart').datetimepicker({
			showSecond: true,
			timeFormat: 'hh:mm:ss',
			stepHour: 2,
			stepMinute: 10,
			stepSecond: 10
		});
		$( "#datepickerstart" ).val(dpStart);
	
	
		var timeShownStart = parseInt($( "#polltimeshown" ).val());
			if(timeShownStart==null || isNaN(timeShownStart)){
				timeShownStart = <%=DemoConstants.getDefaultPollFrequency()%>
				alert('Poll time shown not set, defaulting to "'+timeShownStart+'"');	
				//$( "#polltimeshown" ).val(timeShown);
			}
			timeShownStart = timeShownStart*1000*60;//*24;	
		var start = $( "#datepickerstart" ).datepicker("getDate");
			if(start==null){
				start = new Date();
			}
			var startTimeStart = start.getTime();
			var endTimeStart = startTimeStart+timeShownStart;
			
	
	
	
		 
		var optionsFreq = {
			colors: ["#99CC66", "#999966","#FFCC99","#CCCC99","#996633","#999999","#669999","#663333","#CCCCCC","#6666CC","#663300"],
			lines: { show: true },
			points: { show: false },
			grid: { hoverable: true, clickable: true },
			legend: {position: "ne", margin: [-150,0]},
			xaxis: 
			{	mode:"time",
				timeformat: "%m/%d %H:%M:%S",
				min: (new Date(dpStart)).getTime(),
			},
			yaxis: {
				min: 59.95,
				max: 60.04,
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
			
			
		var dataFreq = [];

		var placeholderFreq = $("#placeholderFreq");
		
		var pollCanceled = false;
			

		var plotFreq = null;
		if(doPlotFreq){
			$.plot(placeholderFreq, dataFreq, optionsFreq);
		}
	    var legendsFreq = $("#placeholderFreq .legendLabel");
		legendsFreq.each(function () {
			// fix the widths so they don't jump around			
			$(this).css('width', $(this).width());
		});
		
		//$.plot(placeholderErrFlags, dataFlags, optionsErr);
		var updateLegendTimeout = null;
		var latestPosition = null;
    
		function updateLegend() {
			updateLegendTimeout = null;        
			var pos = latestPosition;
			
			var j, currentIdx, dataset = dataFlags;//plotEvents.getData();
			var series = dataset[0];
			
			if(series!=null && series.data!=null){
				//alert('series not null');
				// find the nearest points, x-wise
				currentIdx = null;
				for (j = 0; j < series.data.length; ++j){
					if (series.data[j][0] > pos.x)
						break;
					else 	
						currentIdx = j;					
				};
				//todo lookup in the error ones instead and if exists, show error values 
				//$("#errFlagsLabel").text(series.data[currentIdx][0]+"   "+currentIdx+"  "+series.data[currentIdx][1]);
				if(series.data[currentIdx]!=null){
					$("#errFlagsLabel").text(series.data[currentIdx][1]);
				} else {
					$("#errFlagsLabel").text("N/A");
				}
				
				if(dataWN[0]!=null && dataWN[0].data!=null && dataWN[0].data[currentIdx]!=null){
					$("#errWNLabel").text(dataWN[0].data[currentIdx][1]);
				} else {
					$("#errWNLabel").text("N/A");
				}
				if(dataRep[0]!=null && dataRep[0].data!=null && dataRep[0].data[currentIdx]!=null){
					$("#errRepLabel").text(dataRep[0].data[currentIdx][1]);
				} else {
					$("#errRepLabel").text("N/A");
				}
				//if(dataRange[0]!=null && dataRange[0].data!=null && dataRange[0].data[currentIdx]!=null){
				//	$("#errRangeLabel").text(dataRange[0].data[currentIdx][1]);
				//}
				
				
			} else {
				$("#errFlagsLabel").text("N/A");
				$("#errRepLabel").text("N/A");
				$("#errWNLabel").text("N/A");
			}
			//$("#eventsLabel").text(formatDate(new Date(pos.x))+"   "+pos.x);
			$("#eventsLabel").text(formatDate(new Date(pos.x)));
			
		}
		 // fetch one series, adding to what we got
		var alreadyFetched = {};
		
		

		
		var alreadyFetchedFreq = {};
		// find the URL in the link right next to us 
		//var dataurl = button.siblings('a').attr('href');
		var dataurlFreq = 'data/pmu?start='+startTimeStart+'&end='+endTimeStart+'&pmus='+pmuList;
		// then fetch the data with jQuery
		function onFreqDataReceived(series) {
			for(var i=0;i<series.length;i++){
				// let's add it to our current data
				if (!alreadyFetchedFreq[series[i].label]) {				
					alreadyFetchedFreq[series[i].label] = true;
					dataFreq.push(series[i]);
					//alert('adding'+series);
				}
			}
			// and plot all we got
			$.plot(placeholderFreq, dataFreq, optionsFreq);
		 }
		function onError() {
			alert('ajax error occured');
		
		}
		if(doPlotFreq){
			$.ajax({
				url: dataurlFreq,
				method: 'GET',
				dataType: 'json',
				success: onFreqDataReceived,
				error: onError
			});
		}
		
		
		  function showTooltip(x, y, contents) {
			$('<div id="tooltip">' + contents + '</div>').css( {
				position: 'absolute',
				display: 'none',
				top: y + 5,
				left: x + 5,
				border: '1px solid #fdd',
				padding: '2px',
				'background-color': '#fee',
				opacity: 0.80
			}).appendTo("body").fadeIn(200);
		}

		var previousPoint = null;
		
		
		
		$("#placeholderErrFlags").bind("plothover", function (event, pos, item) {
			latestPosition = pos;
			if (!updateLegendTimeout)
				updateLegendTimeout = setTimeout(updateLegend, 50);
		});	
		//$("#placeholderErrRange").bind("plothover", function (event, pos, item) {
		//	latestPosition = pos;
		//	if (!updateLegendTimeout)
		//		updateLegendTimeout = setTimeout(updateLegend, 50);
		//});
		$("#placeholderErrRep").bind("plothover", function (event, pos, item) {
			latestPosition = pos;
			if (!updateLegendTimeout)
				updateLegendTimeout = setTimeout(updateLegend, 50);
		});
		$("#placeholderErrWN").bind("plothover", function (event, pos, item) {
			latestPosition = pos;
			if (!updateLegendTimeout)
				updateLegendTimeout = setTimeout(updateLegend, 50);
		});			
				
		// initiate a recurring data update
		$("input.pollData").click(function () {
			pollCanceled = false;
			$("input.stopPollData").attr('disabled', false);
			$("input.pollData").attr('disabled', true);			
			$("#pollingImg").css( "visibility" , "visible"  );
			
			
			$("#message").html('');
			
			doPlotFreq = true;
			
			var frequency = parseInt($( "#pollfreq" ).val());	
			if(frequency==null || isNaN(frequency)){
				frequency = <%=DemoConstants.getDefaultPollFrequency()%>;
				alert('Poll frequency not set, defaulting to "'+frequency+'"');	
				$( "#pollfreq" ).val(frequency);
			}
			frequency = frequency*1000;		
			var increment = 60*1000;
			/*var increment = parseInt($( "#pollinc" ).val());
			if(increment==null || isNaN(increment)){
				increment = <%=DemoConstants.getDefaultPollIncrement()%>;
				alert('Poll increment not set, defaulting to "'+increment+'"');	
				$( "#pollinc" ).val(increment);
			}	
			increment = increment*1000*60;	
			*/			
			var timeShown = parseInt($( "#polltimeshown" ).val());
			if(timeShown==null || isNaN(timeShown)){
				timeShown = <%=DemoConstants.getDefaultPollTimeShown()%>
				alert('Poll time shown not set, defaulting to "'+timeShown+'"');	
				$( "#polltimeshown" ).val(timeShown);
			}
			timeShown = timeShown*1000*60*60;//*24;			
			
			var start = $( "#datepickerstart" ).datepicker("getDate");
			if(start==null){
				start = new Date();
			}
			var startTime = start.getTime();
			var currentMostRecent = startTime;
			var endTime = startTime+timeShown;
				
			
			
			
			// reset data
			dataFreq = [];
			
			
			
			var iteration = 0;
			
			function fetchData() {
				++iteration;
				
			
				function onDataReceivedFreq(series) {
					dataFreq = [];
					// we get all the data in one go, if we only got partial
					// data, we could merge it with what we already got
										
					//get last known start time to set as new start time
					for(var i=0;i<series.length;i++){
						// let's add it to our current data
						dataFreq.push(series[i]);
					}
					if(startTime>currentMostRecent){
						currentMostRecent = startTime;
					}
					//var mostRecent = getMostRecentTime(dataFreq[0].data);
					//if(mostRecent>currentMostRecent){
					//	currentMostRecent = mostRecent;		
					//	replotErrors();						
					//}
					plotOptionsEvents =  getTimelineOptionsFreq("FF0000", currentMostRecent, timeShown, false, true, false, "hz", 2);
					$.plot($("#placeholderFreq"), dataFreq, optionsFreq);
				}
				
				function onError(data, textStatus, jqXHR) {
					pollCanceled = true;
				 	$("#pollingImg").css( "visibility" , "hidden"  );
					$("input.pollData").attr('disabled', false);
					$("input.stopPollData").attr('disabled', true);	
					if(jqXHR=='Forbidden'){
						$("#message").html('Access for the requested PMUs is denied');
					} else {
						$("#message").html('Unknown error occured while retrieving the data');
					}
				}
				
				
				var selectedGroups  = new Array();
				$("input[@name='checkboxgroup[]']:checked").each(function() {
				    selectedGroups.push($(this).val());
				   
				});
				pmuList = selectedGroups.join();
				if(selectedGroups.length==0){
				 	alert('Please select at least one PMU');
				 	pollCanceled = true;
				 	$("#pollingImg").css( "visibility" , "hidden"  );
					$("input.pollData").attr('disabled', false);
					$("input.stopPollData").attr('disabled', true);		
					return;
				}
				
				
				var freqUrl = 'data/pmu?start='+startTime+'&end='+endTime+'&pmus='+pmuList;
				$.ajax({					
					url: freqUrl ,
					method: 'GET',
					dataType: 'json',
					success: onDataReceivedFreq,
					error: onError
				});
				
				//startTime = currentMostRecent-timeShown;
				if(endTime-startTime>timeShown){
					//alert('changing starttime '+timeShown+'  '+(endTime-startTime));					
					startTime = endTime-timeShown;
				}
				endTime = endTime+increment;
				//alert('showing '+startTime+' to '+endTime);
				var sdate = new Date(startTime);
// 				var sformattedTime = sdate.getHours() + ':' + sdate.getMinutes() + ':' + sdate.getSeconds();
				var edate = new Date(endTime);
// 				var eformattedTime = edate.getHours() + ':' + edate.getMinutes() + ':' + edate.getSeconds();
				//alert('showing '+sformattedTime+' - '+eformattedTime);
				
				if(!pollCanceled){
					setTimeout(fetchData, frequency);					
				} else {
					$("#pollingImg").css( "visibility" , "hidden"  );
					$("input.pollData").attr('disabled', false);
					$("input.stopPollData").attr('disabled', true);					
				}
				
			}

			setTimeout(fetchData, 1000);
		});
		
		$("input.stopPollData").click(function () {
			pollCanceled = true;				
		});
		

	});
		
	</script>
	<table width="100%">
		<tr>
			<td>
				<!--<input class="timelineUpdate" type="button" value="Update Timeline">  -->
		
				<br /> <input class="pollData" type="button" value="Poll for data">
				<input class="stopPollData" type="button" value="Stop Poll for data"
					disabled="true">
				<ul>
					<li>Poll Frequency: <input type="text" id="pollfreq"
						name="pollfreq" value="<%=DemoConstants.getDefaultPollFrequency()%>"> (sec)
					</li>
					<!--<li>Poll Increment: <input type="text" id="pollinc" name="pollinc" value="<%=DemoConstants.getDefaultPollIncrement()%>">    (min)</li>-->
					<li>Poll Time Shown: <input type="text" id="polltimeshown"
						name="polltimeshown" value="<%=DemoConstants.getDefaultPollTimeShown()%>">
						(minutes)
					</li>
					<li>Start: <input type="text" id="datepickerstart" name="datepickerstart">
					</li>
				</ul>
			</td>
			<td>
				<table>
					<tr>
						<td>
							Utility 1
							<ul>
								<li><input type="checkbox" class="checkboxgroup" value="5" checked="checked"/>BE50</li>
								<li><input type="checkbox" class="checkboxgroup" value="6" checked="checked"/>SYLM</li>
								<li><input type="checkbox" class="checkboxgroup" value="7" checked="checked"/>MPLV</li>
							</ul>
						</td>
						
						<td>
							Utility 2
							<ul>
								<li><input type="checkbox" class="checkboxgroup" value="8" />KEEL</li>
								<li><input type="checkbox" class="checkboxgroup" value="9" />CPJK</li>
								<li><input type="checkbox" class="checkboxgroup" value="10" />SUML</li>
							</ul>
						</td>
						<td>
							Utility 3
							<ul>
								<li><input type="checkbox" class="checkboxgroup" value="11" />SLAT</li>
								<li><input type="checkbox" class="checkboxgroup" value="12" />SCE1</li>
								<li><input type="checkbox" class="checkboxgroup" value="14" />MCN2</li>
							</ul>
						</td>
					</tr>
				
				
				</table>
			
			
			</td>
		</tr>
	</table>

	

	<br />
	<div id="message" style="color:red" ></div>
	<div id="placeholderFreq" style="width: 95%; height: 600px"></div>
	<!---->
	<br />



</body>
</html>