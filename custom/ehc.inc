Define ObjectType {
	TriageTrigger
	TestTrigger
	TreatTrigger
}

ControllerIconModel ImageFile { '<res>/images/Controller-256.png' }  Transparent { TRUE }

TriageTrigger JavaClass { ehc.TriageTrigger }  Palette { 'Extended Health Clinic' } DefaultDisplayModel { ControllerIconModel } IconFile { '<res>/images/Controller-24.png' } DefaultSize { 0.5 0.5 0.5 m }
TestTrigger JavaClass { ehc.TestTrigger }  Palette { 'Extended Health Clinic' } DefaultDisplayModel { ControllerIconModel } IconFile { '<res>/images/Controller-24.png' } DefaultSize { 0.5 0.5 0.5 m }
TreatTrigger JavaClass { ehc.TreatTrigger }  Palette { 'Extended Health Clinic' } DefaultDisplayModel { ControllerIconModel } IconFile { '<res>/images/Controller-24.png' } DefaultSize { 0.5 0.5 0.5 m }