Define ObjectType	{ 
	HCCMActiveEntity
	HCCMArriveEvent
	HCCMLeaveEvent
	HCCMEntityDelay
	HCCMControlActivity
	
	ControllerTutorialOne
	ControllerTutorialTwo
	ExampleController
	ControllerEHC
	ControllerEHC_Rosters
}

Define	IconModel  {
	SinkIconModel2
	SourceIconModel2

}
	
SinkIconModel2           ImageFile { '<res>/images/Sink-256.png'         }  Transparent { TRUE }
SourceIconModel2         ImageFile { '<res>/images/Source-256.png'       }  Transparent { TRUE }


HCCMActiveEntity JavaClass { HCCMLibrary.entities.HCCMActiveEntity} Palette {'HCCM Library'}  DefaultDisplayModel { Sphere                   } IconFile { '<res>/images/SimEntity-24.png'       } DefaultSize { 0.5 0.5 0.5 m }
HCCMArriveEvent JavaClass { HCCMLibrary.events.HCCMArriveEvent} Palette {'HCCM Library'} DefaultDisplayModel { SourceIconModel2          } IconFile { '<res>/images/Source-24.png'          }
HCCMLeaveEvent JavaClass { HCCMLibrary.events.HCCMLeaveEvent} Palette {'HCCM Library'} DefaultDisplayModel { SinkIconModel2            } IconFile { '<res>/images/Sink-24.png'            }
HCCMEntityDelay JavaClass { HCCMLibrary.entitydelay.HCCMEntityDelay} Palette {'HCCM Library'}  DefaultDisplayModel { EntityDelayModel         } IconFile { '<res>/images/Line-24.png'            }
HCCMControlActivity JavaClass { HCCMLibrary.controlactivity.HCCMControlActivity} Palette {'HCCM Library'}  DefaultDisplayModel { Rectangle                } IconFile { '<res>/images/EntityContainer-24.png' }


ControllerTutorialOne JavaClass { HCCMLibrary.controllers.ControllerTutorialOne} Palette {'HCCM Controllers'} DefaultDisplayModel { AssembleIconModel }  IconFile { '<res>/images/Assemble-24.png' } DefaultSize { 0.5 0.5 0.5 m }
ControllerTutorialTwo JavaClass { HCCMLibrary.controllers.ControllerTutorialTwo} Palette {'HCCM Controllers'} DefaultDisplayModel { AssembleIconModel }  IconFile { '<res>/images/Assemble-24.png' } DefaultSize { 0.5 0.5 0.5 m }
ExampleController JavaClass { HCCMLibrary.controllers.ExampleController} Palette {'HCCM Controllers'} DefaultDisplayModel { AssembleIconModel }  IconFile { '<res>/images/Assemble-24.png' } DefaultSize { 0.5 0.5 0.5 m }
ControllerEHC JavaClass { HCCMLibrary.controllers.ControllerEHC} Palette {'HCCM Controllers'} DefaultDisplayModel { AssembleIconModel }  IconFile { '<res>/images/Assemble-24.png' } DefaultSize { 0.5 0.5 0.5 m }
ControllerEHC_Rosters JavaClass { HCCMLibrary.controllers.ControllerEHC_Rosters} Palette {'HCCM Controllers'} DefaultDisplayModel { AssembleIconModel }  IconFile { '<res>/images/Assemble-24.png' } DefaultSize { 0.5 0.5 0.5 m }