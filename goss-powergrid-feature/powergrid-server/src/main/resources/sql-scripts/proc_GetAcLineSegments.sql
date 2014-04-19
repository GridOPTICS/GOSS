-- --------------------------------------------------------------------------------
-- Routine DDL
-- Note: comments before and after the routine body will not be stored by the server
-- --------------------------------------------------------------------------------
DELIMITER $$
DROP PROCEDURE IF EXISTS `proc_GetAcLineSegments`$$

CREATE PROCEDURE `proc_GetAcLineSegments`(
	timestep timestamp
)
BEGIN
set @ts = timestep;
-- set @powergridId = 1;

-- Select all branches at a particular timestep.
select lt.timestep, l.lineid, br.branchid, br.mrid as branchmrid, bu.basekv, br.rating, br.status,  lt.p, lt.q, 
	br.frombusnumber, br.tobusnumber,
	(select ssub.substationname from substations ssub, buses bsub where bsub.substationid=ssub.substationid and bsub.busnumber = br.frombusnumber) as fromsubsstationname,
	(select ssub.substationname from substations ssub, buses bsub where bsub.substationid=ssub.substationid and bsub.busnumber = br.tobusnumber) as tosubsstationname,
	(select ssub.areaname from substations ssub, buses bsub where bsub.substationid=ssub.substationid and bsub.busnumber = br.frombusnumber) as fromarea,
	(select ssub.areaname from substations ssub, buses bsub where bsub.substationid=ssub.substationid and bsub.busnumber = br.tobusnumber) as toarea,
	(select ssub.mrid from substations ssub, buses bsub where bsub.substationid=ssub.substationid and bsub.busnumber = br.frombusnumber) as fromsubstationmrid,
	(select ssub.mrid from substations ssub, buses bsub where bsub.substationid=ssub.substationid and bsub.busnumber = br.tobusnumber) as tosubstationmrid
from branches br, buses bu, linetimesteps lt, lines_ l 
where br.frombusnumber = bu.busnumber 
    and l.lineid = lt.lineid 
    and l.branchid = br.branchid 
    and lt.powergridid = br.powergridid 
    and l.powergridid = br.powergridid 
    and lt.timestep = @ts;
END