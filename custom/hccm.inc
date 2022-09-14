Define ObjectType {
	ActiveEntity
	PassiveEntity
	ArriveEvent
	LeaveEvent
	LogicEvent
	ProcessActivity
	WaitActivity
	ControlUnit
	Trigger
	CustomClock
	EventLogger
}

AssembleIconModel ImageFile { '<res>/images/Assemble-256.png' }  Transparent { TRUE }
Define PolylineModel { ProcessActivityModel }

ActiveEntity JavaClass { hccm.entities.ActiveEntity }  Palette { 'HCCM' }  DefaultDisplayModel { Sphere } IconFile { '<res>/images/SimEntity-24.png' } DefaultSize { 0.5 0.5 0.5 m }
PassiveEntity JavaClass { hccm.entities.PassiveEntity }  Palette { 'HCCM' }  DefaultDisplayModel { Rectangle } IconFile { '<res>/images/Shape-24.png' } DefaultSize { 0.5 0.5 0.5 m }
ArriveEvent JavaClass { hccm.events.ArriveEvent }  Palette { 'HCCM' }  DefaultDisplayModel { SourceIconModel } IconFile { '<res>/images/Source-24.png' } DefaultSize { 0.5 0.5 0.5 m }
LeaveEvent JavaClass { hccm.events.LeaveEvent }  Palette { 'HCCM' }  DefaultDisplayModel { SinkIconModel } IconFile { '<res>/images/Sink-24.png' } DefaultSize { 0.5 0.5 0.5 m }
LogicEvent JavaClass { hccm.events.LogicEvent }  Palette { 'HCCM' }  DefaultDisplayModel { ResourceUnitModelDefault } IconFile { '<res>/images/Resource-24.png' } DefaultSize { 0.5 0.5 0.5 m }
ProcessActivity JavaClass { hccm.activities.ProcessActivity }  Palette { 'HCCM' } DefaultDisplayModel { ProcessActivityModel }  IconFile { '<res>/images/Line-24.png' } DefaultSize { 0.5 0.5 0.5 m }
WaitActivity JavaClass { hccm.activities.WaitActivity }  Palette { 'HCCM' } DefaultDisplayModel { Triangle }  IconFile { '<res>/images/Queue-24.png' } DefaultSize { 0.5 0.5 0.5 m }
ControlUnit JavaClass { hccm.controlunits.ControlUnit }  Palette { 'HCCM' } DefaultDisplayModel { AssembleIconModel }  IconFile { '<res>/images/Assemble-24.png' } DefaultSize { 0.5 0.5 0.5 m }
Trigger JavaClass { hccm.controlunits.Trigger }  Palette { 'HCCM' } DefaultDisplayModel { AssembleIconModel }  IconFile { '<res>/images/Assemble-24.png' } DefaultSize { 0.5 0.5 0.5 m }
CustomClock JavaClass { hccm.CustomClock }  Palette { 'HCCM' } DefaultDisplayModel { TextModelDefault }  IconFile { '<res>/images/Controller-24.png' } DefaultSize { 0.5 0.5 0.5 m }
EventLogger JavaClass { hccm.events.EventLogger }  Palette { 'HCCM' } DefaultDisplayModel { EntityLoggerIconModel }  IconFile { '<res>/images/EntityLogger-24.png' } DefaultSize { 0.5 0.5 0.5 m }
