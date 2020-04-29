1) Replace <root>/jaamsim/src/main/resources/resources/inputs/autoload.cfg with <root>/custom/autoload.cfg to add HCCM and SSQ (Single Server Queue) functionality to JaamSim

2) You will then have to configure your Java project as described in section 2.4 of the JaamSim Programming Manual (https://jaamsim.com/docs/JaamSim%20Programming%20Manual%20-%20rev%200.51.pdf) to get the customised version of JaamSim to run in Eclipse
a. You must make both custom and R2_custom source folders in your project to get both HCCM libraries to load when you run JaamSim

3) Once you can run the code then open the <root>/custom/ssq.cfg file and see a Single Server Queue defined using generic HCCM components