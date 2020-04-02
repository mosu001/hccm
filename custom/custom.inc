Define ObjectType	{ 
	HCCMEntityDelay
	HCCMEntityDelayControlledOutput
	HCCMQueue
	HCCMEntitySink
	ControllerHCCM
}

Define	IconModel  {
	SinkIconModel2
}
	
SinkIconModel2           ImageFile { '<res>/images/Sink-256.png'         }  Transparent { TRUE }

HCCMEntityDelay JavaClass { HCCMLibrary.HCCMEntityDelay} Palette {'HCCM Library'}  DefaultDisplayModel { EntityDelayModel         } IconFile { '<res>/images/Line-24.png'            }
HCCMEntityDelayControlledOutput JavaClass { HCCMLibrary.HCCMEntityDelayControlledOutput} Palette {'HCCM Library'}  DefaultDisplayModel { EntityDelayModel         } IconFile { '<res>/images/Line-24.png'            }
HCCMQueue JavaClass { HCCMLibrary.HCCMQueue} Palette {'HCCM Library'}  DefaultDisplayModel { Triangle                 } IconFile { '<res>/images/Queue-24.png'           } DefaultSize { 0.5 0.5 0.0 m }
HCCMEntitySink JavaClass { HCCMLibrary.HCCMEntitySink} Palette {'HCCM Library'} DefaultDisplayModel { SinkIconModel2            } IconFile { '<res>/images/Sink-24.png'            }
ControllerHCCM JavaClass { HCCMLibrary.ControllerHCCM} Palette {'HCCM Library'} DefaultDisplayModel { SinkIconModel2            } IconFile { '<res>/images/Sink-24.png'            }
