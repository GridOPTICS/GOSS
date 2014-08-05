
truncate table dev_north.bustimesteps;
INSERT INTO `dev_north`.`bustimesteps`
(`PowerGridId`,
`TimeStep`,
`BusNumber`,
`Code`,
`Va`,
`Vm`)
SELECT 
bts.`PowerGridId`,
bts.`TimeStep`,
bts.`BusNumber`,
bts.`Code`,
bts.`Va`,
bts.`Vm` 
from northandsouth.bustimesteps bts
	LEFT JOIN dev_north.buses nb ON 
		nb.busnumber = nb.busnumber;

truncate table `dev_north`.`machinetimesteps`;
INSERT INTO `dev_north`.`machinetimesteps`
(`MachineId`,
`PowerGridId`,
`TimeStep`,
`PGen`,
`QGen`,
`Status`)
select
mts.`MachineId`,
mts.`PowerGridId`,
mts.`TimeStep`,
mts.`PGen`,
mts.`QGen`,
mts.`Status`
from northandsouth.machinetimesteps mts
	LEFT JOIN dev_north.machines ms
		ON mts.machineid = ms.machineid;

truncate table `dev_north`.`loadtimesteps`;
INSERT INTO `dev_north`.`loadtimesteps`
(`LoadId`,
`PowerGridId`,
`TimeStep`,
`PLoad`,
`QLoad`)
SELECT lts.`LoadId`,
    lts.`PowerGridId`,
    lts.`TimeStep`,
    lts.`PLoad`,
    lts.`QLoad`
FROM `northandsouth`.`loadtimesteps` lts
	LEFT JOIN dev_north.loads ls
		ON lts.loadid = ls.loadid;

