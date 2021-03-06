<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr"%>

<bean:define id="process" name="information" property="process"/>
<bean:define id="processId" name="process" property="externalId" type="java.lang.String"/>
<bean:define id="name" name="information" property="activityName"/>

<div class="infobox_warning mtop1 mbottom1">
	<bean:define id="value" type="module.finance.util.Money" name="process" property="workingCapital.balance"/>
	<bean:define id="personName" type="java.lang.String" name="process" property="workingCapital.movementResponsible.name"/>
	<bean:message bundle="WORKING_CAPITAL_RESOURCES" key="label.module.workingCapital.refund"
		arg0="<%= value.toFormatString() %>" arg1="<%= personName %>"/>
</div>

<div class="dinline forminline">

	<fr:form action='<%= "/workflowProcessManagement.do?method=process&processId=" + processId + "&activity=" + name %>'>

		<fr:edit id="activityBean" name="information">
			<fr:schema type="module.workingCapital.domain.activity.RequestCapitalActivityInformation" bundle="WORKING_CAPITAL_RESOURCES">
				<fr:slot name="confirmed" key="label.module.workingCapital.confirmed" required="true"/>
				<fr:slot name="paymentMethod" key="label.module.workingCapital.paymentMethod" required="true"/>
			</fr:schema>
			<fr:layout name="tabular">
				<fr:property name="classes" value="form listInsideClear" />
				<fr:property name="columnClasses" value="width100px,,tderror" />
			</fr:layout>
		</fr:edit>

		<html:submit styleClass="inputbutton"><bean:message key="button.submit" bundle="EXPENDITURE_RESOURCES"/> </html:submit>
	
	</fr:form>

	<fr:form action='<%= "/workflowProcessManagement.do?method=viewProcess&processId=" + processId %>'>
		<html:submit styleClass="inputbutton"><bean:message key="renderers.form.cancel.name" bundle="RENDERER_RESOURCES"/> </html:submit>
	</fr:form>

</div>
