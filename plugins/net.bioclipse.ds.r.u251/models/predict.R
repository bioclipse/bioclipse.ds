u251.tmp <- predict(u251.naAndStdvTreatment, %input%)
u251.tmp <- predict(u251.imputed, u251.tmp)
names(u251.tmp) <- rownames(u251.rf$importance)
predict(u251.rf, t(u251.tmp), type="prob")[2]
