-- --------------------------------------------------------------------------------
-- Routine DDL
-- Note: comments before and after the routine body will not be stored by the server
-- --------------------------------------------------------------------------------
DELIMITER $$
DROP PROCEDURE IF EXISTS proc_GetAllContingencies$$

CREATE PROCEDURE `proc_GetAllContingencies`()
BEGIN
	SELECT ctg.*
	FROM contingencies ctg;
END