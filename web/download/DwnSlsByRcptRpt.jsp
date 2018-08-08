<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page language = "java" errorPage = "../Common/ExceptionError.jsp" %>
<%@ page import = "java.util.*" %>
<%@ page import = "qrcom.util.*" %>
<%@ page import = "qrcom.PROFIT.files.info.*" %>
<%@ page import = "qrcom.PROFIT.shared.Default.DefaultCoySub" %>
<%@ page import = "qrcom.PROFIT.files.dao.local.DI.DwnSlsByRcptRptDAO" %>
<%@ page import = "qrcom.PROFIT.webbean.HTTPObj.*" %>
<%@ page import = "qrcom.PROFIT.webbean.language.*" %>
<%@ page import = "qrcom.PROFIT.system.*" %>
<jsp:useBean id="jbSession" scope="session" class="qrcom.PROFIT.servlet.HTTPSessionAttributeWrapper" />
<jsp:useBean id="aduserInfo" scope="session" class="qrcom.PROFIT.files.info.AduserInfo" />
<jsp:useBean id="jbWebSessionParam" scope="session" class="qrcom.PROFIT.webbean.HTTPObj.WebSessionParam" />
<jsp:useBean id="jbBlnHistoryPage" scope="request" class="java.lang.String" />
<% 
    WResGUI jbWResGUI			= jbWebSessionParam.getWResGUI(request);
    WResPrompt jbWResPrompt		= jbWebSessionParam.getWResPrompt(request);
    WebAuthorization webAuth	= new WebAuthorization(request);
    	
    String BaseURL		= SysParam.getInstance().getBaseURL();
    String USR_ID		= jbWebSessionParam.getAduserInfo().USR_ID();
    String USR_TYPE		= jbWebSessionParam.getAduserInfo().USR_TYPE();
    String lang_code	= jbWebSessionParam.getAduserInfo().USR_LANGUAGE();
	
	String SYSStoreLength           	= (String)jbWebSessionParam.getProfitvvValue(request, "SYSStoreLength");
	String SYSHeadOfficeStore          	= (String)jbWebSessionParam.getProfitvvValue(request, "SYSHeadOfficeStore");
    
    String strCOY		= aduserInfo.USR_COMP();
    String strCOY_NAME	= HTTPUtilClass.getDesc(lang_code, "COY_NAME", "COYMST", " WHERE COY = '" + strCOY + "'");
       
    CoysubmstComboBox coysubmstComboBox = new CoysubmstComboBox(request);  
    
    String strAction    	= request.getParameter( "ACTION" );
    String strRptCategory   = request.getParameter( "KEY1" );
    String strCoySub		= request.getParameter( "KEY2" );
    String strStore			= request.getParameter( "KEY3" );
    String strFrGenDate		= request.getParameter( "KEY4" );
    String strToGenDate		= request.getParameter( "KEY5" );
    String strUserID		= request.getParameter( "KEY6" );
    
    
    HParam hParam = new HParam();
    
    
    hParam.put("USR_ID", jbWebSessionParam.getAduserInfo().USR_ID());
    hParam.put("USR_TYPE", jbWebSessionParam.getAduserInfo().USR_TYPE());
    hParam.put("USR_LANGUAGE", jbWebSessionParam.getAduserInfo().USR_LANGUAGE());
    
    if (strAction != null)			hParam.setActionCode(strAction);				else strAction = ""; 
    if (strRptCategory != null)     hParam.put("rptCategory", strRptCategory);     	else strRptCategory = ""; 
    if (strCOY != null)				hParam.put("coy", strCOY);						else strCOY = "";    
    if (strCoySub != null)			hParam.put("coySub", strCoySub);				else strCoySub = "";
    if (strStore != null)			hParam.put("store", strStore);		    		else strStore = "";
    if (strFrGenDate != null)		hParam.put("frGenDate", strFrGenDate);			else strFrGenDate = "";
    if (strToGenDate != null)		hParam.put("toGenDate", strToGenDate);			else strToGenDate = "";
	if (strUserID != null)			hParam.put("userID", strUserID);				else strUserID = "";


	if (strAction != null && strAction.equals("INIT") || strAction.equals("reset"))
    {
		strCoySub =	HTTPUtilClass.getDesc("COY_SUB", "ADOPRCOYSUB", " WHERE USR_ID = '" + USR_ID + "' AND DEFAULT_COY_SUB = 'Y'");
    	USR_TYPE    = jbWebSessionParam.getAduserInfo().USR_TYPE();
    	strRptCategory = "SLSR_DWN";
    }
    
    String settitles = "";
    
    if(strRptCategory.equals("SLSR_DWN"))
    	settitles = jbWResGUI.getRes("DWN_DI_SLSR_RPT");
    %>
<!DOCTYPE html>
<html>
    <head>
        <% 
            Collection err = (Collection)request.getSession().getAttribute("errorSets");
            if (err != null && !err.isEmpty())
            {
            %>
        <script>
            window.open("<%=BaseURL%>/profit/Common/Error.jsp", "", "height=300, width=500, resizable=yes, scrollbars=yes");
        </script>
        <%
            }
            %>
        <jsp:include page="../Common/Header.jsp"/>
        <jsp:include page="../Common/Script.jsp"/>
        <script>
            function restart()
            {
            	'use strict';
            	try
            	{
            		document.FORM.elements['txtDate' + whichOne ].value = '' + year + '-' + padout(month - 0 + 1) + '-' + padout(day);
            		mywindow.close();
            	}
            	catch(e)
            	{
            		alert('<%=jbWResGUI.getRes("Please Try Again")%>');
            	}
            
            	if(whichOne === 1 || whichOne === 2)
            	{
            		checkDATE();
            	}
            }
            
            function checkDATE()
            {
            	'use strict';
            	var FrGenDate	= window.document.FORM.txtDate1.value;
            	var ToGenDate	= window.document.FORM.txtDate2.value;
            	
            	if(FrGenDate !== "" && ToGenDate !== "")
            	{
            		if(!compareDate(FrGenDate,"LE",ToGenDate))
            		{
            			alert('<%=jbWResGUI.getRes("To Generate Date must be greater than From Generate Date")%>');
            			window.document.FORM.txtDate2.value = "";
            			setTimeout("window.document.FORM.txtDate2.focus()", 100);
            			return false;
            		}
            	}
            	return;
            	
            } 
            
            function checkFromTo()
            {
            	'use strict';
            	var errorFrTo	= 0;
            	var errorMsg	= "";
            	var FrGenDate	= window.document.FORM.txtDate1.value;
            	var ToGenDate	= window.document.FORM.txtDate2.value;
            
            	if(FrGenDate !== "" && ToGenDate === "")
            	{
            		errorFrTo++;
            	}
            
            	if(FrGenDate === "" && ToGenDate !== "")
            	{
            		errorFrTo++;
            	}
            
            	if(errorFrTo !== 0)
            	{
            		alert('<%=jbWResPrompt.getRes("TO_FROM_FIELD_MUST_ENTER")%>');
            		return;
            	}
            	
            	setFormSubmit('<%=BaseURL%>/profit/download/DwnSlsByRcptRpt.jsp','S','RPT_CATEGORY%cboCOY_SUB%txtStore%txtDate1%txtDate2%txtUserID');
            
            }
			
			function SearchCriteria(PATH, search_action)
			{
				'use strict';
				window.document.FORM.SEARCH_ACTION.value = search_action;
				
				if (search_action == 'STORE')
				{
					PATH += '&KEY2='+document.FORM.cboCOY_SUB.value+'&FILTER1=store in (select store from adoprstr where usr_id=\'<%=USR_ID%>\') and store_del_cd=\'N\'';	
				}
				
				window.open(PATH,'null','height=600,width=800,left=0,top=0,status=no,toolbar=no,menubar=no,location=no,resizable=yes,scrollbars=yes');
			}
			
			function returnValue(strReturn)
			{
				'use strict';
				var search_action = window.document.FORM.SEARCH_ACTION.value; 
				
				if (search_action == 'STORE')
				{
					window.document.FORM.txtStore.value = getValue('STORE',strReturn);
					onChangeStore(getValue('STORE',strReturn));
				}
				if(window.document.FORM.SEARCH_ACTION.value == "txtUserID")
				{
					window.document.FORM.txtUserID.value = getValue("USR_ID", strReturn);
				}
			}
			
			function onChangeStore(store)
			{
				'use strict';
				var TargetClass    = 'qrcom.PROFIT.webbean.dao.MT.DefaultStrmstWebBean';
				var TargetMethod   = 'onChangeAuthorisedStoreByCoySub';
				var CallbackMethod = 'onChangeStoreCalledBack';
			
				var Params  = '&STORE=' + store;
				Params += '&COY_SUB='+document.FORM.cboCOY_SUB.value;
			
				var baseURL = '<%=BaseURL%>';
				invokeTargetClass(baseURL, TargetClass, TargetMethod, CallbackMethod, Params);
			}
   
			function onChangeStoreCalledBack(strReturn)
			{
				'use strict';
				var ret_status = getValue('@STATUS', strReturn);
				var store = document.FORM.txtStore.value;
				checkException(strReturn);
				
				
				if (ret_status == 'N')
				{
					alert('<%=jbWResPrompt.getRes("Store")%>' + ' ' + '<%=jbWResPrompt.getRes("NE_REC")%>');
					document.FORM.txtStore.value = '';
					focusField('txtStore'); 
				}
				else if (store == '<%=SYSHeadOfficeStore%>')
				{
					alert('<%=jbWResPrompt.getRes("Head Office Store code is not allowed.")%>');
					document.FORM.txtStore.value = '';
					focusField('txtStore');
				}
			}
            
            function processHTTPPage(strPageAction)
            {
            	'use strict';
            	var ACTION = strPageAction;
            	var URLstr = '<%=BaseURL%>/profit/download/DwnSlsByRcptRpt.jsp'; 
            
            	if (strPageAction === 'getPage')
            	{
            		ACTION += "&PAGE=" + window.document.FORM.txtGotoPage.value;		
            	}
            	window.document.FORM_SUBMIT.action = buildActionURL("FORM", URLstr, ACTION, "RPT_CATEGORY%cboCOY_SUB%txtStore%txtDate1%txtDate2%txtUserID");
            	window.document.FORM_SUBMIT.submit();
            }
            
            function resetPage()
            {
            	'use strict';
            	var url = "<%=BaseURL%>/profit/download/DwnSlsByRcptRpt.jsp";
            	setFormReset(window.document.FORM,url,'');
            
            }
            
        </script>
    </head>
    <% if (err != null && !err.isEmpty()) { %>
    <body>
	<% } else { %>
	<body onLoad="setFocus();">
		<% } %>
		<center>
			<FORM id="FORM" method="post" action="<%=BaseURL%>/profit/download/DwnSlsByRcptRpt.jsp" name="FORM">
				<fieldset class="fieldsettitle">
					<legend><%=settitles%></legend>
					<table width="100%" cellpadding="7" cellspacing="0" border="0" class="table-border">
						<tr>
							<td>
								<table border="0" width="100%" cellspacing="1" cellpadding="3" class="table-border">
									<th><%=jbWResGUI.getRes("Search Criteria")%></th>
									<tr>
										<td>
											<table border="0" width="100%" cellspacing="0" cellpadding="3">
												<tr>
													<td width="15%" class="caption"><%=jbWResGUI.getRes("Company")%></td>
													<td width="5%"><input type="text" name="txtCOY" value="<%=strCOY%>" readonly tabindex="-1" class="input-display"></td>
													<td width="25%"><input type="text" name="txtCOY_NAME" value="<%=strCOY_NAME%>" readonly tabindex="-1" class="input-display"></td>
													<td width="5%" colspan="1"></td>
													<td width="15%" class="caption"><%=jbWResGUI.getRes("Subsidiary")%></td>
													<td width="30%" >
														<select name="cboCOY_SUB" class="mandatory">
														<%=coysubmstComboBox.getAuthorisedCoySubComboBox(strCoySub)%>
														</select>
													</td>
													<td width="5%"></td>
												</tr>
												<tr>
													<td width="15%" class="caption"><%=jbWResGUI.getRes("Store")%></td>
													<td width="30%" colspan="2"><input onchange="if(this.value!='')onChangeStore(this.value);" name="txtStore" value="<%=strStore%>" maxlength="<%=SYSStoreLength%>"></td>
													<td width="5%"><img src="<%=BaseURL%>/profit/images/search.gif" name="imgStore" alt="<%=jbWResGUI.getRes("Find")%>" onclick="SearchCriteria('<%=BaseURL%>/profit/MT/StrmstSearch.jsp?ACTION=search','STORE')" style='cursor:hand;'></td>
													<td width="15%" colspan="3"></td>
												</tr>
												<tr>
													<td width="15%" class="caption"><%=jbWResGUI.getRes("From Generate Date")%></td>
													<td width="30%" colspan="2" ><input type="text" name="txtDate1" value="<%=strFrGenDate%>" onchange="this.value=formatDate(this.value);checkDATE();" ></td>
													<td width="5%"><img src="<%=BaseURL%>/profit/images/calendr.gif" name="imgCalendar1"  alt="<%=jbWResGUI.getRes("Calender")%>" onclick=newWindow(1) style='cursor:hand'></td>
													<td width="15%" class="caption"><%=jbWResGUI.getRes("To Generate Date")%></td>
													<td width="30%"><input type="text" name="txtDate2" value="<%=strToGenDate%>" onchange="this.value=formatDate(this.value);checkDATE();"></td>
													<td width="5%"><img src="<%=BaseURL%>/profit/images/calendr.gif" name="imgCalendar2" alt="<%=jbWResGUI.getRes("Calender")%>" onclick=newWindow(2) style='cursor:hand'></td>
												</tr>
												<tr>
													<td width="15%" class="caption"><%=jbWResGUI.getRes("User ID")%></td>
													<td width="30%" colspan="2"><input type="text" name="txtUserID" value="<%=strUserID%>" maxlength="8"/></td>
													<td width="5%"><img src="<%=BaseURL%>/profit/images/search.gif" name="imgCreateBy" ALT="<%=jbWResGUI.getRes("Find")%>" onclick="SearchCriteria('<%=BaseURL%>/profit/AD/AduserSearch.jsp?ACTION=search', 'txtUserID')" style='cursor:hand;'></td>
													<td width="15%"></td>
													<td width="30%"></td>
													<td width="5%"></td>
												</tr>
												<tr>
													<td colspan="7">
														<table width="100%" cellpadding="0" cellspacing="1">
															<tr>
																<td width="76%">&nbsp;</td>
																<td width="12%"><input type="reset" name="cmdReset" value="<%=jbWResGUI.getRes("Reset")%>" onclick="resetPage()"></td>
																<td width="12%"><input type="button" name="cmdSearch" value="<%=jbWResGUI.getRes("Search")%>" onclick="if(Validate(this.form,'search','','Y'))checkFromTo();"></td>
															</tr>
														</table>
													</td>
												</tr>
											</table>
										</td>
									</tr>
								</table>
							</td>
						</tr>
					</table>
					<table border="0" width="100%" cellspacing="1" cellpadding="3" class="table-border">
						<tr>
							<th><%=jbWResGUI.getRes("Search Results")%></th>
						</tr>
						<tr>
							<td>
								<table width='100%' border="0" cellspacing="1" cellpadding="0" class="tb-list">
									<tr>
										<td class="tb-display" width="15%"><%=jbWResGUI.getRes("Generate Date")%></td>
										<td class="tb-display" width="40%"><%=jbWResGUI.getRes("Timestamp: (MMDDHHMMSS)")%></td>
										<td class="tb-display" width="15%"><%=jbWResGUI.getRes("User ID")%></td>
										<td class="tb-display" width="15%"><%=jbWResGUI.getRes("Store")%></td>
										<td class="tb-display" width="15%"><%=jbWResGUI.getRes("Download")%></td>
									</tr>
									<%
										int i = 0;
										int pageNo  = 0;
										Collection rsCllt = null;
										
										String strLinePerPage = jbWebSessionParam.getProfitvvValue(request, "SYSMaxLinesPerPage");
										DwnSlsByRcptRptDAO dwnSlsByRcptRptDAO	= new DwnSlsByRcptRptDAO();
										   
										   // PageControl.jsp needs this string variable.
										   String strPageOfTotal = "";
										   /* add new codes */			
										String strTdBgColor = "tb-display";
										String strInputColor = "input-display";
										/* end of new codes */
										
										   if (strAction != null && strAction.equals("search"))
										   {
												HTTPPageQueryBased httpPageQueryBased = new HTTPPageQueryBased(hParam, dwnSlsByRcptRptDAO, strLinePerPage);
												jbSession.setAttribute("dwnSlsByRcptRptHttpPageQueryBased", httpPageQueryBased);
												rsCllt         = httpPageQueryBased.getFirstPage();
											 strPageOfTotal = httpPageQueryBased.getPageOfTotal(); 
											 pageNo         = httpPageQueryBased.getCurrPageNo();      
										   }
										   else
										   if (strAction != null && 
												(strAction.equals("getNextPage") ||
												 strAction.equals("getPreviousPage") ||
												 strAction.equals("getFirstPage") ||
												 strAction.equals("getLastPage"))) 
										   {
												HTTPPageQueryBased httpPageQueryBased = (HTTPPageQueryBased)jbSession.getAttribute("dwnSlsByRcptRptHttpPageQueryBased");
												if (httpPageQueryBased != null) 
												{
													rsCllt         = httpPageQueryBased.process(strAction);
													strPageOfTotal = httpPageQueryBased.getPageOfTotal();
													pageNo         = httpPageQueryBased.getCurrPageNo();
												}
										   }
										   else
										   if (strAction != null && strAction.equals("getPage"))
										   {
												HTTPPageQueryBased httpPageQueryBased = (HTTPPageQueryBased)jbSession.getAttribute("dwnSlsByRcptRptHttpPageQueryBased");         
												if (httpPageQueryBased != null) 
												{
													String str = request.getParameter( "PAGE" );
													rsCllt         = httpPageQueryBased.getPage(str);
													strPageOfTotal = httpPageQueryBased.getPageOfTotal();
													pageNo         = httpPageQueryBased.getCurrPageNo();
												}
										   }
										   
										   if (rsCllt != null)
										   {
												Iterator iter = rsCllt.iterator();
										
												String [] str;
												while (iter.hasNext())
												{
												   i++;
												   /* add new codes */			
												   if (strTdBgColor == "tb-display") {
													strTdBgColor = "tb-display2";
													strInputColor = "input-display2";
												   } else {
													strTdBgColor = "tb-display";
													strInputColor = "input-display";
												   }
												/* end of new codes */
												   str = (String [])iter.next();
														   
												if(str[0] == null)		str[0] = "";
												if(str[1] == null)		str[1] = "";
												if(str[2] == null)		str[2] = "";
												if(str[3] == null)		str[3] = "";
												if(str[4] == null)		str[4] = "";
												if(str[5] == null)		str[4] = "";
												
										%> 
									<tr>
										<td class="<%=strTdBgColor%>"><input type="text" name="txtGenDate_<%=i%>" value="<%=qrMisc.discardTime(str[0])%>" readonly tabindex="-1" class="<%=strInputColor%>"></td>
										<td class="<%=strTdBgColor%>"><input type="text" name="txtTimeStamp_<%=i%>" value="<%=str[1]%>" readonly tabindex="-1" class="<%=strInputColor%>"></td>
										<td class="<%=strTdBgColor%>"><input type="text" name="txtUserID_<%=i%>" value="<%=str[4]%>" readonly tabindex="-1" class="<%=strInputColor%>"></td>
										<td class="<%=strTdBgColor%>"><input type="text" name="txtStore_<%=i%>" value="<%=str[3]%>" readonly tabindex="-1" class="<%=strInputColor%>"></td>
										<td class="<%=strTdBgColor%>"><a href="<%=BaseURL%>/servlet/SvltEODReport?ACTION=view&INTF_PATH=<%=str[5]%>&OPEN_TYPE=attachment" target="_self"><%=jbWResGUI.getRes("Download")%></a></td>
									</tr>
									<%		
										}
										}
										%>
								</table>
							</td>
						</tr>
					</table>
					<%@ include file="/profit/Common/PageControl.jsp" %>       
				</fieldset>
				<input type="hidden" name="ACTION" id="ACTION">	
				<input type="hidden" name="SEARCH_ACTION" id="SEARCH_ACTION">
				<input type="hidden" name="SEARCH_FIELD" id="SEARCH_FIELD">
				<input type="hidden" name="RPT_CATEGORY" id="RPT_CATEGORY" value="<%=strRptCategory%>"> 
				<input type="hidden" name="tittle" id="tittle" value="<%=settitles%>">
			</form>
			<FORM id="FORM_SUBMIT" name="FORM_SUBMIT" method="post" action="<%=BaseURL%>/profit/download/DwnSlsByRcptRpt.jsp">
			</FORM>
		</center>
    </body>
</html>