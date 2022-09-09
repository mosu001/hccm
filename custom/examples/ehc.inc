Define ObjectType {
	TriageTrigger
	TestTrigger
	TreatTrigger
        ExtTriageTrigger
        ExtTestTrigger
        ExtTreatTrigger
}

ControllerIconModel ImageFile { '<res>/images/Controller-256.png' }  Transparent { TRUE }

TriageTrigger JavaClass { ehc.TriageTrigger }  Palette { 'Extended Health Clinic' } DefaultDisplayModel { ControllerIconModel } IconFile { '<res>/images/Controller-24.png' } DefaultSize { 0.5 0.5 0.5 m }
TestTrigger JavaClass { ehc.TestTrigger }  Palette { 'Extended Health Clinic' } DefaultDisplayModel { ControllerIconModel } IconFile { '<res>/images/Controller-24.png' } DefaultSize { 0.5 0.5 0.5 m }
TreatTrigger JavaClass { ehc.TreatTrigger }  Palette { 'Extended Health Clinic' } DefaultDisplayModel { ControllerIconModel } IconFile { '<res>/images/Controller-24.png' } DefaultSize { 0.5 0.5 0.5 m }
ExtTriageTrigger JavaClass { ehc.ExtTriageTrigger }  Palette { 'Extended Health Clinic' } DefaultDisplayModel { ControllerIconModel } IconFile { '<res>/images/Controller-24.png' } DefaultSize { 0.5 0.5 0.5 m }
ExtTestTrigger JavaClass { ehc.ExtTestTrigger }  Palette { 'Extended Health Clinic' } DefaultDisplayModel { ControllerIconModel } IconFile { '<res>/images/Controller-24.png' } DefaultSize { 0.5 0.5 0.5 m }
ExtTreatTrigger JavaClass { ehc.ExtTreatTrigger }  Palette { 'Extended Health Clinic' } DefaultDisplayModel { ControllerIconModel } IconFile { '<res>/images/Controller-24.png' } DefaultSize { 0.5 0.5 0.5 m }