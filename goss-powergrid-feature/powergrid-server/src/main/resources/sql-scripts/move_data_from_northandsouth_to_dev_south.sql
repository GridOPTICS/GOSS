
truncate table dev_south.bustimesteps;
INSERT INTO `dev_south`.`bustimesteps`
(`PowerGridId`,
`TimeStep`,
`BusNumber`,
`Code`,
`Va`,
`Vm`)
SELECT distinct
bts.`PowerGridId`,
bts.`TimeStep`,
bts.`BusNumber`,
bts.`Code`,
bts.`Va`,
bts.`Vm` 
from northandsouth.bustimesteps bts
	LEFT JOIN dev_south.buses nb ON 
		nb.busnumber = nb.busnumber;

truncate table `dev_south`.`machinetimesteps`;
INSERT INTO `dev_south`.`machinetimesteps`
(`MachineId`,
`PowerGridId`,
`TimeStep`,
`PGen`,
`QGen`,
`Status`)
select distinct
mts.`MachineId`,
mts.`PowerGridId`,
mts.`TimeStep`,
mts.`PGen`,
mts.`QGen`,
mts.`Status`
from northandsouth.machinetimesteps mts
	LEFT JOIN dev_south.machines ms
		ON mts.machineid = ms.machineid;

truncate table `dev_south`.`loadtimesteps`;
INSERT INTO `dev_south`.`loadtimesteps`
(`LoadId`,
`PowerGridId`,
`TimeStep`,
`PLoad`,
`QLoad`)
SELECT distinct lts.`LoadId`,
    lts.`PowerGridId`,
    lts.`TimeStep`,
    lts.`PLoad`,
    lts.`QLoad`
FROM `northandsouth`.`loadtimesteps` lts
	LEFT JOIN dev_south.loads ls
		ON lts.loadid = ls.loadid;
