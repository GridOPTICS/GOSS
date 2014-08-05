-- --------------------------------------------------------------------------------
-- Routine DDL
-- Note: comments before and after the routine body will not be stored by the server
-- --------------------------------------------------------------------------------
DELIMITER $$

DROP PROCEDURE IF EXISTS `proc_GetContingencyBranchViolationsAtTimestepCim`$$

CREATE PROCEDURE `proc_GetContingencyBranchViolationsAtTimestepCim`(
	IN atTimestep timestamp
	)
BEGIN

	select cbt.contingencyId, c.name, cbt.branchId, mb.mrid, (cbt.voltage/b.Rating) as value
	from contingencybranchviolationtimesteps cbt, mridbranches mb, contingencies c, branches b
		where mb.branchid = cbt.BranchId
	    and mb.BranchId = b.BranchId
	    and b.BranchId = cbt.BranchId
	   	and mb.powergridid = cbt.powergridid 
		and c.contingencyid = cbt.contingencyid
		and c.powergridid = cbt.powergridid
		and c.powergridid = mb.powergridid
		and cbt.timestep = atTimestep
		and (cbt.voltage/b.Rating) >1
		order by cbt.contingencyid, cbt.branchid;

	
END