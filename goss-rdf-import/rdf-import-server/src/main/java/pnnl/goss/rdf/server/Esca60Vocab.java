package pnnl.goss.rdf.server;

import java.io.File;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

public class Esca60Vocab {

	protected static final String uri ="http://fpgi.pnnl.gov/esca60";

	/** returns the URI for this schema
	 * @return the URI for this schema
	 */

	public static String getURI() {
		return uri;
	}

	public static Model readModel(File modelData){
		FileManager.get().readModel(m, modelData.getAbsolutePath());
		return m;
	}

	private static Model m = ModelFactory.createDefaultModel();

	private final static String uri0 = "http://iec.ch/TC57/2007/CIM-schema-cim12#";

	public final static Resource SUBSTATION_OBJECT = m.createProperty(uri0 + "Substation");
	public final static Resource SWITCH_OBJECT = m.createProperty(uri0 + "Switch");
	public final static Resource MEASUREMENT_OBJECT = m.createProperty(uri0 + "Measurement");
	public final static Resource BASEVOLTAGE_OBJECT = m.createProperty(uri0 + "BaseVoltage");
	public final static Resource CURVEDATA_OBJECT = m.createProperty(uri0 + "CurveData");
	public final static Resource TYPE_OBJECT = m.createProperty(uri0 + "type");
	public final static Resource TAPCHANGER_OBJECT = m.createProperty(uri0 + "TapChanger");
	public final static Resource IDENTIFIEDOBJECT_OBJECT = m.createProperty(uri0 + "IdentifiedObject");
	public final static Resource LOADGROUP_OBJECT = m.createProperty(uri0 + "LoadGroup");
	public final static Resource REGULATINGCONDEQ_OBJECT = m.createProperty(uri0 + "RegulatingCondEq");
	public final static Resource REGULARTIMEPOINT_OBJECT = m.createProperty(uri0 + "RegularTimePoint");
	public final static Resource ANALOGLIMITSET_OBJECT = m.createProperty(uri0 + "AnalogLimitSet");
	public final static Resource TRANSFORMERWINDING_OBJECT = m.createProperty(uri0 + "TransformerWinding");
	public final static Resource SERIESCOMPENSATOR_OBJECT = m.createProperty(uri0 + "SeriesCompensator");
	public final static Resource STATICVARCOMPENSATOR_OBJECT = m.createProperty(uri0 + "StaticVarCompensator");
	public final static Resource SHUNTCOMPENSATOR_OBJECT = m.createProperty(uri0 + "ShuntCompensator");
	public final static Resource BREAKER_OBJECT = m.createProperty(uri0 + "Breaker");
	public final static Resource CONDUCTINGEQUIPMENT_OBJECT = m.createProperty(uri0 + "ConductingEquipment");
	public final static Resource BASICINTERVALSCHEDULE_OBJECT = m.createProperty(uri0 + "BasicIntervalSchedule");
	public final static Resource EQUIPMENT_OBJECT = m.createProperty(uri0 + "Equipment");
	public final static Resource IEC61970CIMVERSION_OBJECT = m.createProperty(uri0 + "IEC61970CIMVersion");
	public final static Resource SYNCHRONOUSMACHINE_OBJECT = m.createProperty(uri0 + "SynchronousMachine");
	public final static Resource TERMINAL_OBJECT = m.createProperty(uri0 + "Terminal");
	public final static Resource CONNECTIVITYNODE_OBJECT = m.createProperty(uri0 + "ConnectivityNode");
	public final static Resource CONFORMLOAD_OBJECT = m.createProperty(uri0 + "ConformLoad");
	public final static Resource POWERTRANSFORMER_OBJECT = m.createProperty(uri0 + "PowerTransformer");
	public final static Resource ANALOG_OBJECT = m.createProperty(uri0 + "Analog");
	public final static Resource GROSSTONETACTIVEPOWERCURVE_OBJECT = m.createProperty(uri0 + "GrossToNetActivePowerCurve");
	public final static Resource GENERATINGUNIT_OBJECT = m.createProperty(uri0 + "GeneratingUnit");
	public final static Resource ENERGYCONSUMER_OBJECT = m.createProperty(uri0 + "EnergyConsumer");
	public final static Resource VOLTAGELEVEL_OBJECT = m.createProperty(uri0 + "VoltageLevel");
	public final static Resource CURVE_OBJECT = m.createProperty(uri0 + "Curve");
	public final static Resource CONDUCTOR_OBJECT = m.createProperty(uri0 + "Conductor");
	public final static Resource LIMITSET_OBJECT = m.createProperty(uri0 + "LimitSet");
	public final static Resource SUBGEOGRAPHICALREGION_OBJECT = m.createProperty(uri0 + "SubGeographicalRegion");
	public final static Resource ANALOGLIMIT_OBJECT = m.createProperty(uri0 + "AnalogLimit");
	public final static Resource LINE_OBJECT = m.createProperty(uri0 + "Line");
	public final static Property MEASUREMENT_MEMBEROF_PSR = m.createProperty(uri0 + "Measurement.MemberOf_PSR");
	public final static Property IEC61970CIMVERSION_DATE = m.createProperty(uri0 + "IEC61970CIMVersion.date");
	public final static Property STATICVARCOMPENSATOR_SVCCONTROLMODE = m.createProperty(uri0 + "StaticVarCompensator.sVCControlMode");
	public final static Property LINE_REGION = m.createProperty(uri0 + "Line.Region");
	public final static Property IDENTIFIEDOBJECT_DESCRIPTION = m.createProperty(uri0 + "IdentifiedObject.description");
	public final static Property ENERGYCONSUMER_QFIXEDPCT = m.createProperty(uri0 + "EnergyConsumer.qfixedPct");
	public final static Property ENERGYCONSUMER_QFIXED = m.createProperty(uri0 + "EnergyConsumer.qfixed");
	public final static Property CONDUCTOR_B0CH = m.createProperty(uri0 + "Conductor.b0ch");
	public final static Property VOLTAGELEVEL_MEMBEROF_SUBSTATION = m.createProperty(uri0 + "VoltageLevel.MemberOf_Substation");
	public final static Property CURVEDATA_CURVESCHEDULE = m.createProperty(uri0 + "CurveData.CurveSchedule");
	public final static Property IDENTIFIEDOBJECT_ALIASNAME = m.createProperty(uri0 + "IdentifiedObject.aliasName");
	public final static Property SWITCH_NORMALOPEN = m.createProperty(uri0 + "Switch.normalOpen");
	public final static Property GENERATINGUNIT_MAXOPERATINGP = m.createProperty(uri0 + "GeneratingUnit.maxOperatingP");
	public final static Property TAPCHANGER_STEPVOLTAGEINCREMENT = m.createProperty(uri0 + "TapChanger.stepVoltageIncrement");
	public final static Property TAPCHANGER_NORMALSTEP = m.createProperty(uri0 + "TapChanger.normalStep");
	public final static Property SYNCHRONOUSMACHINE_MEMBEROF_GENERATINGUNIT = m.createProperty(uri0 + "SynchronousMachine.MemberOf_GeneratingUnit");
	public final static Property ENERGYCONSUMER_QFEXP = m.createProperty(uri0 + "EnergyConsumer.qFexp");
	public final static Property SYNCHRONOUSMACHINE_MAXQ = m.createProperty(uri0 + "SynchronousMachine.maxQ");
	public final static Property ENERGYCONSUMER_QVEXP = m.createProperty(uri0 + "EnergyConsumer.qVexp");
	public final static Property TRANSFORMERWINDING_MEMBEROF_POWERTRANSFORMER = m.createProperty(uri0 + "TransformerWinding.MemberOf_PowerTransformer");
	public final static Property GENERATINGUNIT_MINECONOMICP = m.createProperty(uri0 + "GeneratingUnit.minEconomicP");
	public final static Property SHUNTCOMPENSATOR_MAXIMUMSECTIONS = m.createProperty(uri0 + "ShuntCompensator.maximumSections");
	public final static Property REGULARTIMEPOINT_INTERVALSCHEDULE = m.createProperty(uri0 + "RegularTimePoint.IntervalSchedule");
	public final static Property TRANSFORMERWINDING_WINDINGTYPE = m.createProperty(uri0 + "TransformerWinding.windingType");
	public final static Property GENERATINGUNIT_RATEDGROSSMAXP = m.createProperty(uri0 + "GeneratingUnit.ratedGrossMaxP");
	public final static Property ANALOG_NORMALVALUE = m.createProperty(uri0 + "Analog.normalValue");
	public final static Property GENERATINGUNIT_SHORTPF = m.createProperty(uri0 + "GeneratingUnit.shortPF");
	public final static Property CONDUCTOR_R0 = m.createProperty(uri0 + "Conductor.r0");
	public final static Property SUBSTATION_REGION = m.createProperty(uri0 + "Substation.Region");
	public final static Property STATICVARCOMPENSATOR_CAPACITIVERATING = m.createProperty(uri0 + "StaticVarCompensator.capacitiveRating");
	public final static Property CONDUCTOR_BCH = m.createProperty(uri0 + "Conductor.bch");
	public final static Property GENERATINGUNIT_MAXECONOMICP = m.createProperty(uri0 + "GeneratingUnit.maxEconomicP");
	public final static Property ENERGYCONSUMER_PFIXED = m.createProperty(uri0 + "EnergyConsumer.pfixed");
	public final static Property TRANSFORMERWINDING_B = m.createProperty(uri0 + "TransformerWinding.b");
	public final static Property IEC61970CIMVERSION_VERSION = m.createProperty(uri0 + "IEC61970CIMVersion.version");
	public final static Property ANALOGLIMIT_LIMITSET = m.createProperty(uri0 + "AnalogLimit.LimitSet");
	public final static Property TRANSFORMERWINDING_G = m.createProperty(uri0 + "TransformerWinding.g");
	public final static Property GENERATINGUNIT_MINOPERATINGP = m.createProperty(uri0 + "GeneratingUnit.minOperatingP");
	public final static Property TRANSFORMERWINDING_RATEDU = m.createProperty(uri0 + "TransformerWinding.ratedU");
	public final static Property TRANSFORMERWINDING_RATEDS = m.createProperty(uri0 + "TransformerWinding.ratedS");
	public final static Property SHUNTCOMPENSATOR_REACTIVEPERSECTION = m.createProperty(uri0 + "ShuntCompensator.reactivePerSection");
	public final static Property STATICVARCOMPENSATOR_INDUCTIVERATING = m.createProperty(uri0 + "StaticVarCompensator.inductiveRating");
	public final static Property CONNECTIVITYNODE_MEMBEROF_EQUIPMENTCONTAINER = m.createProperty(uri0 + "ConnectivityNode.MemberOf_EquipmentContainer");
	public final static Property LOADGROUP_SUBLOADAREA = m.createProperty(uri0 + "LoadGroup.SubLoadArea");
	public final static Property ENERGYCONSUMER_PFIXEDPCT = m.createProperty(uri0 + "EnergyConsumer.pfixedPct");
	public final static Property STATICVARCOMPENSATOR_VOLTAGESETPOINT = m.createProperty(uri0 + "StaticVarCompensator.voltageSetPoint");
	public final static Property SHUNTCOMPENSATOR_NOMU = m.createProperty(uri0 + "ShuntCompensator.nomU");
	public final static Property CURVEDATA_Y1VALUE = m.createProperty(uri0 + "CurveData.y1value");
	public final static Property TRANSFORMERWINDING_R = m.createProperty(uri0 + "TransformerWinding.r");
	public final static Property CURVEDATA_XVALUE = m.createProperty(uri0 + "CurveData.xvalue");
	public final static Property SYNCHRONOUSMACHINE_INITIALREACTIVECAPABILITYCURVE = m.createProperty(uri0 + "SynchronousMachine.InitialReactiveCapabilityCurve");
	public final static Property MEASUREMENT_TERMINAL = m.createProperty(uri0 + "Measurement.Terminal");
	public final static Property TAPCHANGER_STEPPHASESHIFTINCREMENT = m.createProperty(uri0 + "TapChanger.stepPhaseShiftIncrement");
	public final static Property EQUIPMENT_MEMBEROF_EQUIPMENTCONTAINER = m.createProperty(uri0 + "Equipment.MemberOf_EquipmentContainer");
	public final static Property TRANSFORMERWINDING_X = m.createProperty(uri0 + "TransformerWinding.x");
	public final static Property TERMINAL_CONDUCTINGEQUIPMENT = m.createProperty(uri0 + "Terminal.ConductingEquipment");
	public final static Property GENERATINGUNIT_RATEDGROSSMINP = m.createProperty(uri0 + "GeneratingUnit.ratedGrossMinP");
	public final static Property CONDUCTOR_X0 = m.createProperty(uri0 + "Conductor.x0");
	public final static Property ENERGYCONSUMER_PFEXP = m.createProperty(uri0 + "EnergyConsumer.pFexp");
	public final static Property TAPCHANGER_NEUTRALSTEP = m.createProperty(uri0 + "TapChanger.neutralStep");
	public final static Property TAPCHANGER_TCULCONTROLMODE = m.createProperty(uri0 + "TapChanger.tculControlMode");
	public final static Property SYNCHRONOUSMACHINE_REFERENCEPRIORITY = m.createProperty(uri0 + "SynchronousMachine.referencePriority");
	public final static Property TYPE = m.createProperty(uri0 + "type");
	public final static Property TAPCHANGER_HIGHSTEP = m.createProperty(uri0 + "TapChanger.highStep");
	public final static Property CURVEDATA_Y2VALUE = m.createProperty(uri0 + "CurveData.y2value");
	public final static Property SERIESCOMPENSATOR_R = m.createProperty(uri0 + "SeriesCompensator.r");
	public final static Property REGULARTIMEPOINT_VALUE2 = m.createProperty(uri0 + "RegularTimePoint.value2");
	public final static Property GENERATINGUNIT_RATEDNETMAXP = m.createProperty(uri0 + "GeneratingUnit.ratedNetMaxP");
	public final static Property REGULARTIMEPOINT_VALUE1 = m.createProperty(uri0 + "RegularTimePoint.value1");
	public final static Property LIMITSET_ISPERCENTAGELIMITS = m.createProperty(uri0 + "LimitSet.isPercentageLimits");
	public final static Property MEASUREMENT_MEASUREMENTTYPE = m.createProperty(uri0 + "Measurement.MeasurementType");
	public final static Property BASEVOLTAGE_NOMINALVOLTAGE = m.createProperty(uri0 + "BaseVoltage.nominalVoltage");
	public final static Property SYNCHRONOUSMACHINE_RATEDS = m.createProperty(uri0 + "SynchronousMachine.ratedS");
	public final static Property TAPCHANGER_REGULATIONSCHEDULE = m.createProperty(uri0 + "TapChanger.RegulationSchedule");
	public final static Property TAPCHANGER_LOWSTEP = m.createProperty(uri0 + "TapChanger.lowStep");
	public final static Property SHUNTCOMPENSATOR_NORMALSECTIONS = m.createProperty(uri0 + "ShuntCompensator.normalSections");
	public final static Property CONDUCTINGEQUIPMENT_BASEVOLTAGE = m.createProperty(uri0 + "ConductingEquipment.BaseVoltage");
	public final static Property TAPCHANGER_TRANSFORMERWINDING = m.createProperty(uri0 + "TapChanger.TransformerWinding");
	public final static Property GENERATINGUNIT_NORMALPF = m.createProperty(uri0 + "GeneratingUnit.normalPF");
	public final static Property SYNCHRONOUSMACHINE_TYPE = m.createProperty(uri0 + "SynchronousMachine.type");
	public final static Property IDENTIFIEDOBJECT_PATHNAME = m.createProperty(uri0 + "IdentifiedObject.pathName");
	public final static Property SYNCHRONOUSMACHINE_MINQ = m.createProperty(uri0 + "SynchronousMachine.minQ");
	public final static Property CONDUCTOR_X = m.createProperty(uri0 + "Conductor.x");
	public final static Property SUBGEOGRAPHICALREGION_REGION = m.createProperty(uri0 + "SubGeographicalRegion.Region");
	public final static Property GROSSTONETACTIVEPOWERCURVE_GENERATINGUNIT = m.createProperty(uri0 + "GrossToNetActivePowerCurve.GeneratingUnit");
	public final static Property ANALOG_POSITIVEFLOWIN = m.createProperty(uri0 + "Analog.positiveFlowIn");
	public final static Property STATICVARCOMPENSATOR_SLOPE = m.createProperty(uri0 + "StaticVarCompensator.slope");
	public final static Property CONFORMLOAD_LOADGROUP = m.createProperty(uri0 + "ConformLoad.LoadGroup");
	public final static Property CONDUCTOR_R = m.createProperty(uri0 + "Conductor.r");
	public final static Property CURVE_CURVESTYLE = m.createProperty(uri0 + "Curve.curveStyle");
	public final static Property VOLTAGELEVEL_BASEVOLTAGE = m.createProperty(uri0 + "VoltageLevel.BaseVoltage");
	public final static Property POWERTRANSFORMER_TRANSFORMERTYPE = m.createProperty(uri0 + "PowerTransformer.transformerType");
	public final static Property IDENTIFIEDOBJECT_NAME = m.createProperty(uri0 + "IdentifiedObject.name");
	public final static Property GENERATINGUNIT_LONGPF = m.createProperty(uri0 + "GeneratingUnit.longPF");
	public final static Property CONDUCTOR_LENGTH = m.createProperty(uri0 + "Conductor.length");
	public final static Property SERIESCOMPENSATOR_X = m.createProperty(uri0 + "SeriesCompensator.x");
	public final static Property CONDUCTOR_GCH = m.createProperty(uri0 + "Conductor.gch");
	public final static Property REGULATINGCONDEQ_REGULATIONSCHEDULE = m.createProperty(uri0 + "RegulatingCondEq.RegulationSchedule");
	public final static Property TERMINAL_CONNECTIVITYNODE = m.createProperty(uri0 + "Terminal.ConnectivityNode");
	public final static Property ANALOGLIMITSET_MEASUREMENTS = m.createProperty(uri0 + "AnalogLimitSet.Measurements");
	public final static Property ENERGYCONSUMER_PVEXP = m.createProperty(uri0 + "EnergyConsumer.pVexp");
	public final static Property ANALOGLIMIT_VALUE = m.createProperty(uri0 + "AnalogLimit.value");
	public final static Property BASICINTERVALSCHEDULE_STARTTIME = m.createProperty(uri0 + "BasicIntervalSchedule.startTime");
	public final static Property BREAKER_RATEDCURRENT = m.createProperty(uri0 + "Breaker.ratedCurrent");
}