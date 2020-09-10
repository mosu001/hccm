setwd("C:\\Users\\phant\\Desktop\\hccm\\R2_custom")
#Lab4data = read.table("Lab4_ExtendedSHC.dat", sep="\t",
#                      col.names=c("WalkUp.TimeInSystem", "WalkUp.WaitTriage", "WalkUp.WaitTest", "WalkUp.WaitTreat",
#                                  "Scheduled.TimeInSystem", "Scheduled.WaitTreat", "TriageNurse.Utilization", 
#                                  "TestNurse.Utilization", "Doctor1.Utilisation", "Doctor2.Utilisation"
#                      ), skip=1)
Lab4data = read.table("HCCM_EHC2.dat", sep="\t",
                        col.names=c("WalkUp.TimeInSystem", "WalkUp.WaitTriage", "WalkUp.WaitTest", "WalkUp.WaitTreat",
                                    "Scheduled.TimeInSystem", "Scheduled.WaitTreat", "TriageNurse.Utilization", 
                                    "TestNurse.Utilization", "Doctor1.Utilisation", "Doctor2.Utilisation"
                        ), skip=1)

Lab4scen = Lab4data[1:dim(Lab4data)[2]]

Lab4scen2 = apply(Lab4scen, 2, FUN=function(x) {
  xnew<-sub("\\[h\\]","",x)
  xnew = as.double(xnew)
})

results = apply(Lab4scen2, 2, FUN = function(x) {
  tTest = t.test(x)
  return(tTest$conf.int)
})

temp = results[2,]
results[2,] = apply(Lab4scen2, 2, mean)
results = rbind(results, temp)
rownames(results) = c("ConfLower", "Mean", "ConfUpper")
t(results)
