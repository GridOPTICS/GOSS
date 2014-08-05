-- --------------------------------------------------------------------------------
-- Routine DDL
-- Note: comments before and after the routine body will not be stored by the server
-- --------------------------------------------------------------------------------
DELIMITER $$

DROP PROCEDURE IF EXISTS `proc_GetContingencyOutOfServiceBranchesCim`$$

CREATE PROCEDURE `proc_GetContingencyOutOfServiceBranchesCim`(
	contingencyId int
)
BEGIN
	-- Returns the branches that are out for the specified contingencyId
	SELECT m.mrid, c.branchid 
	FROM mridbranches m, contingencybranchesout c
	WHERE m.branchid = c.branchid 
		AND c.contingencyId = contingencyId;

END