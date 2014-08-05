-- --------------------------------------------------------------------------------
-- Routine DDL
-- Note: comments before and after the routine body will not be stored by the server
-- --------------------------------------------------------------------------------
DELIMITER $$

DROP FUNCTION IF EXISTS `create_mrid`$$

CREATE FUNCTION `create_mrid`(elementid INTEGER, datatype varchar(20)) RETURNS char(36) CHARSET utf8
BEGIN
	-- Create a uuid 
	SET @mrid := '';
	SET @present := TRUE;

	WHILE @present DO
		SET @mrid := UUID();
		
		SET @present := EXISTS(SELECT Mrid FROM globalmrid WHERE Mrid = @mrid);

	END WHILE;
	
	
	INSERT INTO globalmrid (Mrid, DataType) VALUES (@mrid, datatype);
RETURN @mrid;
END