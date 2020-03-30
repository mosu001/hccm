require(ggplot2)

setwd("~/orua/simmod/hccm-code/custom")

df <- read.delim("ssq_abm-TransmissionLogger.log", skip = 11)
colnames(df) = c("Time", "Num.Transmissions", "Num.Cust.Cus", "Num.Cust.Serv", "Num.Serv.Cust")
plot_df = melt(data = df, id.vars = c("Time"), measure.vars = c("Num.Transmissions", "Num.Cust.Cus", "Num.Cust.Serv", "Num.Serv.Cust"))
ggplot(df, aes(x=df$Time)) + geom_point(aes(y=df$Num.Transmissions))
