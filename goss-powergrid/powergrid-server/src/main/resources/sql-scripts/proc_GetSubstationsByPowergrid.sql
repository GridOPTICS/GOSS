-- --------------------------------------------------------------------------------
-- Routine DDL
-- Note: comments before and after the routine body will not be stored by the server
-- --------------------------------------------------------------------------------
DELIMITER $$

DROP PROCEDURE if exists proc_GetSubstations$$

CREATE PROCEDURE `proc_GetSubstations`(
	in timestep timestamp
)
BEGIN

set @timestep = timestep;

select s.substationid,
	s.substationname,
	s.latitude,
	s.longitude,
	s.mrid as substationmrid,
	a.mrid as areamrid,
	a.areaname,
	loadAndGen.*,
	sqrt(loadAndGen.sumMaxPGen*loadAndGen.sumMaxPGen+loadAndGen.sumMaxQGen*loadAndGen.sumMaxQGen) as totalMaxMVar
FROM areas a, substations s 
	LEFT JOIN
		(select b.substationid as loadsubstationid, sum(l.pload) as sumPLoad, sum(l.qload) as sumQLoad,
			gen.*
		from loads l, loadtimesteps lt, buses b
		left join 
			(select b.substationid as machinesubstationid, sum(m.maxpgen) as sumMaxPGen, sum(m.maxqgen) as sumMaxQGen, sum(mt.pgen) as sumPGen, sum(mt.qgen) as sumQGen
			from buses b, machines m, machinetimesteps mt
			where b.busnumber = m.busnumber
				AND m.machineid = mt.machineid
				AND mt.timestep = @timestep
			group by b.substationid) gen
		on gen.machinesubstationid = b.SubstationId
		where b.busnumber = l.busnumber
			AND l.loadid = lt.loadid
			AND lt.timestep = @timestep
			AND b.substationid = gen.machinesubstationid
		group by b.substationid) as loadAndGen
	ON s.substationid = loadAndGen.loadsubstationid
WHERE s.areaname = a.areaname;

END