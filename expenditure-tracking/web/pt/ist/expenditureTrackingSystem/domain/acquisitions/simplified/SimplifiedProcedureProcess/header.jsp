<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr" %>


<bean:define id="currentState" name="process" property="acquisitionProcessStateType"/>
<fr:view name="process"> 
	<fr:layout name="process-state">
		<fr:property name="stateParameterName" value="state"/>
		<fr:property name="contextRelative" value="true"/>
		<fr:property name="currentStateClass" value=""/>
		<fr:property name="linkable" value="true"/>
	</fr:layout>
</fr:view>
	
<div class="wrapper">

<h2>
	<bean:message key="acquisitionProcess.title.viewAcquisitionRequest" bundle="ACQUISITION_RESOURCES"/>
	<span class="processNumber">(<fr:view name="process" property="acquisitionRequest.acquisitionProcessId"/>)</span>	
</h2> 


<logic:equal name="process" property="warnRegardingProcessClassificationNeeded" value="true">
	 <div class="infobox_warning mtop15">
	 	<p class="mvert025">
	         <bean:message key="label.warning.mismatchBetweenClassificationAndUnitDefault" bundle="ACQUISITION_RESOURCES"/>
	         
	         <logic:equal name="process" property="acquisitionRequest.requestingUnit.defaultRegeimIsCCP" value="true">
	        	<p>
	        	<strong>
	        			<bean:message key="label.warning.mismatchBetweenClassificationAndUnitDefault.ccpWarn" bundle="ACQUISITION_RESOURCES"/>
	        	</strong>
	        	</p>
	         </logic:equal>
	    </p>
	</div>
</logic:equal>

<logic:equal name="process" property="warnForLessSuppliersActive" value="true">
	<div class="infobox_warning mtop15">
	 	<p class="mvert025">
	 		<bean:message key="label.warning.warnForLessSuppliers" bundle="ACQUISITION_RESOURCES"/>
	 	</p>
	 </div>
</logic:equal>
</div>