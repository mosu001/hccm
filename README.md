# HCCM Library for JaamSim

## Introduction

JaamSim is open source Java software for discrete-event simulation (DES). Hierarchical Control Conceptual Modelling is a paradigm for DES modelling that explicitly defines the control of entities flowing through a DES. Multiple models exist that successfully implement HCCM in JaamSim in an ad hoc manner, but there is no consistent implementation of HCCM concepts in JaamSim. JaamSim is also easily customisable so the potential for an HCCM “plug-in” exists.

This project will develop a prototype HCCM plug-in for JaamSim. This plug-in will be applied to develop new versions of two existing “ad hoc” HCCM simulation models – one for construction, the other for healthcare. The existing implementations of the simulation models will be used to validate the new plug-in components.

## Setup Notes

1) Replace \<root>/jaamsim/src/main/resources/resources/inputs/autoload.cfg with \<root>/custom/autoload.cfg to add HCCM and SSQ (Single Server Queue) functionality to JaamSim

2) You will then have to configure your Java project as described in section 2.4 of the [JaamSim Programming Manual](https://jaamsim.com/docs/JaamSim%20Programming%20Manual%20-%20rev%200.51.pdf) to get the customised version of JaamSim to run in Eclipse
a. You must make both custom and R2_custom source folders in your project to get both HCCM libraries to load when you run JaamSim

3) Once you can run the code then open the \<root>/custom/ssq.cfg file and see a Single Server Queue defined using generic HCCM components

## Issues to resolve

- MO 2020/05/15 - HCCMLibrary seems to me to only allows an active and a passive entity in a ControlActivity, but activities are often two or more active entities

## Changelog

## Useful Links

- [Project Description](https://part4project.foe.auckland.ac.nz/home/project/detail/2804/)
- [JaamSim Programming Manual](https://jaamsim.com/docs/JaamSim%20Programming%20Manual%20-%20rev%200.51.pdf)
- [JaamSim User Manual](https://jaamsim.com/docs/JaamSim%20User%20Manual%202017-10.pdf)
