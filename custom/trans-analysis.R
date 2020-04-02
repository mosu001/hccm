require(ggplot2)
require(reshape2)

setwd("~/orua/simmod/hccm-code/custom")

df <- read.delim("ssq_abm-TransmissionLogger.log", skip = 11)
colnames(df) = c("Time", "Num.Transmissions", "Num.Cust.Cus", "Num.Cust.Serv", "Num.Serv.Cust")
plot_df = melt(data = df, id.vars = c("Time"), measure.vars = c("Num.Transmissions", "Num.Cust.Cus", "Num.Cust.Serv", "Num.Serv.Cust"))
ggplot(plot_df, aes(x=Time, y=value, color=variable)) + geom_point() +
  ylim(0, 110)
