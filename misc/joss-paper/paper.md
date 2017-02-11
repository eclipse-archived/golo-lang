---
title: "Eclipse Golo"
tags:
  - golo
  - java
  - jvm
  - language
  - invokedynamic
  - jsr292
authors:
  - name: Julien Ponge
    orcid: 0000-0003-3452-9183
    affiliation: 1
  - name: Yannick Loiseau
    affiliation: 2
  - name: Frédéric Le Mouël
    affiliation: 1
  - name: Nicolas Stouls
    affiliation: 1
  - name: Philippe Charrière
    affiliation: 6
  - name: Daniel Petisme
    affiliation: 3
  - name: Sylvain Desgrais
    affiliation: 4
  - name: Franck Verrot
    affiliation: 5
affiliations:
  - name: Univ Lyon, INSA Lyon, CITI, F-69621 Villeurbanne, France
    index: 1
  - name: Blaise Pascal University, LIMOS, F-63170 Aubière, France
    index: 2
  - name: Manufacture Française des Pneumatiques Michelin
    index: 3
  - name: Almerys
    index: 4
  - name: Omada Health, Inc.
    index: 5
  - name: GitHub, Inc.
    index: 6
date: 23 September 2016
bibliography: paper.bib
---

# Summary

Golo is a simple dynamically-typed programming language for the Java Virtual Machine (JVM) that has
been designed to leverage the capabilities of the Java 7 invokedynamic bytecode instruction and
java.lang.invoke API (JSR 292) [@Ponge:2013:GDL:2500828.2500844] [@Thalinger:2010:OI:1852761.1852763]. Coupled with a minimal runtime that directly uses the Java SE API,
Golo is an interesting language for rapid prototyping, polyglot application embedding, research
(e.g., runtime extensions, language prototyping) and teaching (e.g., programming, dynamic language
runtime implementation) [@Maingret:2015:TDC:2786545.2786552] [@DBLP:journals/corr/PongeMSL15].

# References
