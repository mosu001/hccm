# HCCM Library for JaamSim

<!-- TOC -->

- [HCCM Library for JaamSim](#hccm-library-for-jaamsim)
	- [Introduction](#introduction)
	- [Setup Notes](#setup-notes)
	- [Issues to resolve](#issues-to-resolve)
	- [Useful Links](#useful-links)
	- [Changelog](#changelog)

<!-- /TOC -->

## Introduction

JaamSim is open source Java software for discrete-event simulation (DES). Hierarchical Control Conceptual Modelling is a paradigm for DES modelling that explicitly defines the control of entities flowing through a DES. Multiple models exist that successfully implement HCCM in JaamSim in an ad hoc manner, but there is no consistent implementation of HCCM concepts in JaamSim. JaamSim is also easily customisable so the potential for an HCCM “plug-in” exists.

This project will develop a prototype HCCM plug-in for JaamSim. This plug-in will be applied to develop new versions of two existing “ad hoc” HCCM simulation models – one for construction, the other for healthcare. The existing implementations of the simulation models will be used to validate the new plug-in components.

## Setup Notes

Also see *Setup Guide*.

1) Replace \<root>/jaamsim/src/main/resources/resources/inputs/autoload.cfg with \<root>/custom/autoload.cfg to add HCCM and SSQ (Single Server Queue) functionality to JaamSim

2) You will then have to configure your Java project as described in section 2.4 of the [JaamSim Programming Manual](https://jaamsim.com/docs/JaamSim%20Programming%20Manual%20-%20rev%200.51.pdf) to get the customised version of JaamSim to run in Eclipse
a. You must make both custom and R2_custom source folders in your project to get both HCCM libraries to load when you run JaamSim

3) Once you can run the code then open the \<root>/custom/ssq.cfg file and see a Single Server Queue defined using generic HCCM components

## Issues to resolve

- MO 2020/05/15 - HCCMLibrary seems to me to only allows an active and a passive entity in a ControlActivity, but activities are often two or more active entities

## Useful Links

- [Project Description](https://part4project.foe.auckland.ac.nz/home/project/detail/2804/)
- [JaamSim Programming Manual](https://jaamsim.com/docs/JaamSim%20Programming%20Manual%20-%20rev%200.51.pdf)
- [JaamSim User Manual](https://jaamsim.com/docs/JaamSim%20User%20Manual%202017-10.pdf)

## Changelog

**20/05/20**

- Added new healthcare related icons
- Updates to documentation

**18/05/20**

- Updates to documentation

**16/05/20**

- Fixes for ProcessActivity and ssq_abm in custom
- Other assorted fixes for custom

**15/05/20**

- Updates to ABMTrigger
- Changed ControlActivity to ProcessActivity

**13/05/20**

- First drafts of new documentation

**30/04/20**

- Updates to R2_custom

**22/04/20**

- Added basic Javadoc comments for custom

**8/04/20**

- Added new HCCM implementation (R2_custom, Rick and Rick)

**03/04/20**

- Added .cfg for SHC

**02/04/20**

- Updated autoload.cfg
- Updated to JaamSim 02-2020

**30/03/20**

- Added ABM to the custom SSQ model

**29/03/20**

- Updated to JaamSim 06-2019
- Updated ssq.cfg

**28/03/20**

- Initial commit of HCCM library (custom)
- Added Jaamsim repository as a dependency
