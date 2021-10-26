-keep class app.family.Settings, app.family.church, app.family.Podium # for R8.fullMode
-keepclassmembernames class app.family.Settings, app.family.Settings$Tree, app.family.Settings$Diagram, app.family.Settings$ZippedTree, app.family.Settings$Share { *; }
-keepclassmembers class org.folg.gedcom.model.* { *; }
#-keeppackagenames org.folg.gedcom.model # Gedcom parser lo chiama come stringa eppure funziona anche senza
-keepnames class org.slf4j.LoggerFactory
-keep class org.jdom.input.* { *; } # per GeoNames
-keepattributes LineNumberTable,SourceFile # per avere i numeri di linea corretti in Android vitals

#-printusage build/usage.txt # risorse che vengono rimosse
#-printseeds build/seeds.txt # entrypoints
