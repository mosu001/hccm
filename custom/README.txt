*NOTE* I am assuming you are in the custom folder of the HCCM GitHub repo (since this is where thei README file is).

1) Replace ../jaamsim/src/main/resources/resources/inputs/autoload.cfg with autload.cfg to add HCCM and SSQ (Single Server Queue) functionality to JaamSim
2) You will have to configure your Java project as described in section 2.4 of the JaamSim Programming Manual (https://jaamsim.com/docs/JaamSim%20Programming%20Manual%20-%20rev%200.51.pdf) to get the customised version of JaamSim to run in Eclipse
3) Once you can run the code then open the ssq.cfg file and see a Single Server Queue defined using generic HCCM components