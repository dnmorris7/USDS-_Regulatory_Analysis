package com.usds.regulations.service;

import com.usds.regulations.entity.Regulation;
import com.usds.regulations.entity.RegulationRelationship;
import com.usds.regulations.entity.RelationshipType;
import com.usds.regulations.entity.ConflictSeverity;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MockDataService {
    
    private final Random random = new Random();
    
    /**
     * Generate realistic mock regulations for testing with varied content
     */
    public List<Regulation> generateMockRegulations(Integer titleNumber, Integer count) {
        List<Regulation> mockRegulations = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            // Use varied content generation for more realistic data
            Regulation regulation = createVariedMockRegulation(titleNumber, String.valueOf(i));
            mockRegulations.add(regulation);
        }
        
        return mockRegulations;
    }
    
    /**
     * Create a single mock regulation with realistic content
     */
    public Regulation createMockRegulation(Integer titleNumber, String partNumber) {
        Regulation regulation = new Regulation();
        
        // Basic identifiers
        regulation.setCfrTitle(titleNumber);
        regulation.setPartNumber(partNumber);
        regulation.setAgencyName(getAgencyForTitle(titleNumber));
        
        // Generate realistic title and content
        String mockTitle = generateMockTitle(titleNumber, partNumber);
        String mockContent = generateMockContent(titleNumber, partNumber);
        
        regulation.setTitle(mockTitle);
        regulation.setContent(mockContent);
        regulation.setWordCount(calculateWordCount(mockContent));
        regulation.setContentChecksum(generateChecksum(mockContent));
        
        // Mock amendment dates
        LocalDate baseDate = LocalDate.of(2020, 1, 1);
        regulation.setLatestAmendedOn(baseDate.plusDays(random.nextInt(1000)));
        regulation.setLatestIssueDate(baseDate.plusDays(random.nextInt(800)));
        regulation.setLastUpdatedOn(baseDate.plusDays(random.nextInt(1200)));
        regulation.setAmendmentCount(random.nextInt(10) + 1);
        regulation.setEcfrLastModified(LocalDateTime.now().minusDays(random.nextInt(365)));
        
        // Source URL
        regulation.setSourceUrl("https://ecfr.federalregister.gov/api/versioner/v1/structure/2025-09-18/title-" + 
                                titleNumber + ".json");
        
        return regulation;
    }
    
    /**
     * Generate realistic regulation titles based on CFR title
     */
    private String generateMockTitle(Integer titleNumber, String partNumber) {
        
        List<String> titleTemplates = switch (titleNumber) {
            case 7 -> List.of(
                "Agricultural Product Standards and Regulations for Part " + partNumber,
                "Food Safety and Inspection Requirements - Part " + partNumber,
                "Organic Certification Standards - Part " + partNumber,
                "Livestock and Poultry Regulations - Part " + partNumber,
                "Conservation Program Guidelines - Part " + partNumber,
                "Commodity Credit Corporation Programs - Part " + partNumber,
                "Agricultural Marketing Service Standards - Part " + partNumber,
                "Forest Service Environmental Regulations - Part " + partNumber
            );
            case 10 -> List.of(
                "Energy Efficiency Standards for Part " + partNumber,
                "Nuclear Regulatory Guidelines - Part " + partNumber,
                "Renewable Energy Certification - Part " + partNumber,
                "Power Plant Safety Requirements - Part " + partNumber,
                "Environmental Impact Assessments - Part " + partNumber,
                "Energy Conservation Program Standards - Part " + partNumber,
                "Federal Energy Regulatory Commission Rules - Part " + partNumber,
                "Strategic Petroleum Reserve Operations - Part " + partNumber
            );
            case 21 -> List.of(
                "Food and Drug Administration Guidelines for Part " + partNumber,
                "Medical Device Safety Standards - Part " + partNumber,
                "Pharmaceutical Manufacturing Requirements - Part " + partNumber,
                "Clinical Trial Regulations - Part " + partNumber,
                "Food Labeling Requirements - Part " + partNumber,
                "Biological Products Regulations - Part " + partNumber,
                "Tobacco Product Standards - Part " + partNumber,
                "Dietary Supplement Regulations - Part " + partNumber
            );
            case 40 -> List.of(
                "Environmental Protection Standards for Part " + partNumber,
                "Air Quality Monitoring Requirements - Part " + partNumber,
                "Water Pollution Control Regulations - Part " + partNumber,
                "Waste Management Guidelines - Part " + partNumber,
                "Chemical Safety Standards - Part " + partNumber,
                "Pesticide Registration Requirements - Part " + partNumber,
                "Clean Air Act Implementation - Part " + partNumber,
                "Superfund Site Remediation Standards - Part " + partNumber
            );
            default -> List.of(
                getTitleName(titleNumber) + " - Regulatory Requirements for Part " + partNumber,
                "General Provisions and Standards - Part " + partNumber,
                "Administrative Procedures and Guidelines - Part " + partNumber,
                "Compliance and Enforcement Requirements - Part " + partNumber,
                "Implementation Standards - Part " + partNumber,
                "Federal Oversight Procedures - Part " + partNumber
            );
        };
        
        return titleTemplates.get(random.nextInt(titleTemplates.size()));
    }
    
    /**
     * Generate realistic regulation content with proper section structure
     */
    private String generateMockContent(Integer titleNumber, String partNumber) {
        StringBuilder content = new StringBuilder();
        
        // Section headers and content based on CFR title
        String[] sections = switch (titleNumber) {
            case 7 -> new String[]{
                "SECTION " + partNumber + ".1 GENERAL PROVISIONS",
                "This part establishes comprehensive standards for agricultural products, ensuring quality, safety, and compliance with federal regulations. The following requirements apply to all producers, processors, and distributors operating within the scope of this regulation.",
                
                "SECTION " + partNumber + ".2 DEFINITIONS AND SCOPE", 
                "For purposes of this part: (a) 'Agricultural product' means any commodity or product, raw or processed, that is marketed for human food or livestock feed. (b) 'Producer' means any person engaged in the business of growing or producing agricultural products for commercial distribution.",
                
                "SECTION " + partNumber + ".3 QUALITY STANDARDS",
                "All agricultural products must meet established quality standards including proper handling procedures, storage requirements, and transportation guidelines that ensure product safety during distribution.",
                
                "SECTION " + partNumber + ".4 INSPECTION PROCEDURES",
                "Regular inspections will be conducted by authorized personnel to ensure compliance with established standards. Certification requirements include annual facility inspections, product testing for quality parameters, and documentation review of production records.",
                
                "SECTION " + partNumber + ".5 ENFORCEMENT AND PENALTIES",
                "Violations may result in penalties including monetary fines, suspension of certification, or prohibition from interstate commerce. The severity of penalties will be determined based on the nature of the violation and compliance history.",
                
                "SECTION " + partNumber + ".6 RECORD KEEPING REQUIREMENTS",
                "Producers must maintain detailed records of production practices, input usage, harvest dates, and post-harvest handling procedures. Records must be retained for a minimum of three years and made available for inspection upon request.",
                
                "SECTION " + partNumber + ".7 EMERGENCY PROCEDURES",
                "Emergency response procedures must be established for food safety incidents, contamination events, or other circumstances that may compromise product integrity. Immediate notification requirements apply for certain categories of incidents."
            };
            case 10 -> new String[]{
                "SECTION " + partNumber + ".1 ENERGY EFFICIENCY STANDARDS",
                "These regulations establish mandatory energy efficiency requirements for equipment and facilities to reduce energy consumption and environmental impact while maintaining operational effectiveness. The standards apply to all covered equipment and are designed to promote technological innovation, reduce operating costs, and support national energy security objectives. Compliance with these standards is mandatory for all applicable equipment manufactured or imported after the effective date.",
                
                "SECTION " + partNumber + ".2 PERFORMANCE CRITERIA AND TESTING",
                "All covered equipment must achieve minimum efficiency ratings as specified in the technical standards appendix. Testing procedures must follow established protocols published by recognized standards organizations and be conducted by certified laboratories with appropriate accreditation. Test results must be verified through independent third-party validation and reported using standardized formats and methodologies.",
                
                "SECTION " + partNumber + ".3 COMPLIANCE MONITORING AND REPORTING",
                "Regular monitoring and reporting requirements ensure ongoing compliance with efficiency standards. Annual reports must be submitted documenting energy usage patterns, efficiency measures implemented, improvement plans for the following year, and verification of continued compliance with applicable standards. Monitoring systems must be calibrated and maintained according to manufacturer specifications.",
                
                "SECTION " + partNumber + ".4 IMPLEMENTATION AND EFFECTIVE DATES",
                "These regulations become effective 180 days after publication in the Federal Register. Existing equipment has a phase-in period of three years to achieve compliance through retrofitting or replacement. New equipment must comply immediately upon the effective date. Transitional provisions are available for equipment under construction or on order prior to the effective date."
            };
            case 21 -> new String[]{
                "SECTION " + partNumber + ".1 FOOD AND DRUG SAFETY OVERVIEW",
                "This regulation ensures the safety and efficacy of food products and pharmaceutical drugs through comprehensive oversight, testing requirements, and manufacturing standards. The Food and Drug Administration has authority to inspect facilities, review data, and take enforcement action to protect public health. All covered products must undergo appropriate review and approval processes before entering interstate commerce.",
                
                "SECTION " + partNumber + ".2 GOOD MANUFACTURING PRACTICES",
                "Good Manufacturing Practices (GMP) must be followed at all production facilities. This includes proper sanitation procedures to prevent contamination, quality control procedures to ensure product consistency, personnel training programs to maintain competency, equipment maintenance protocols to ensure proper operation, and environmental monitoring to detect potential hazards. Documentation of all GMP activities must be maintained and available for inspection.",
                
                "SECTION " + partNumber + ".3 LABELING AND ADVERTISING REQUIREMENTS",
                "All products must include accurate labeling with ingredient lists, nutritional information where applicable, allergen warnings as required, and appropriate safety instructions for consumer use. Labels must comply with standardized formatting requirements and be approved by FDA before use. Advertising claims must be substantiated by adequate scientific evidence and not be false or misleading.",
                
                "SECTION " + partNumber + ".4 ADVERSE EVENT REPORTING",
                "Manufacturers must establish systems for collecting, evaluating, and reporting adverse events associated with their products. Serious adverse events must be reported to FDA within specified timeframes. Risk evaluation and mitigation strategies may be required for products with identified safety concerns. Post-market surveillance activities must be conducted to monitor product safety.",
                
                "SECTION " + partNumber + ".5 INSPECTION AND ENFORCEMENT",
                "FDA may conduct inspections of facilities, records, and products to ensure compliance with applicable regulations. Inspection findings that reveal violations may result in warning letters, consent decrees, product recalls, or other enforcement actions. Criminal prosecution may be pursued for willful violations that pose significant public health risks."
            };
            case 40 -> new String[]{
                "SECTION " + partNumber + ".1 ENVIRONMENTAL PROTECTION STANDARDS",
                "This regulation establishes environmental protection standards to safeguard air and water quality, prevent pollution, and protect human health and the environment. The Environmental Protection Agency has authority to implement and enforce these standards through permits, monitoring, and enforcement actions. All regulated entities must comply with applicable requirements and obtain necessary permits before commencing operations.",
                
                "SECTION " + partNumber + ".2 EMISSION LIMITS AND MONITORING",
                "Emission limits are established for pollutants that may harm human health or the environment. Continuous emission monitoring systems must be installed and operated according to technical specifications. Data must be recorded, maintained, and reported to EPA according to established schedules. Quality assurance procedures must be followed to ensure data accuracy and reliability.",
                
                "SECTION " + partNumber + ".3 PERMIT REQUIREMENTS AND PROCEDURES",
                "Permits are required for activities that may result in environmental releases. Permit applications must include detailed information about proposed activities, emission estimates, control measures, and monitoring plans. Public participation opportunities are provided during the permit review process. Permits include specific conditions and requirements that must be met to maintain authorization.",
                
                "SECTION " + partNumber + ".4 COMPLIANCE AND ENFORCEMENT",
                "Regular inspections and reviews are conducted to ensure compliance with permit conditions and regulatory requirements. Violations may result in notices of violation, administrative orders, civil penalties, or criminal prosecution depending on the severity and circumstances. Compliance assistance is available to help regulated entities understand and meet their obligations."
            };
            default -> new String[]{
                "SECTION " + partNumber + ".1 GENERAL REQUIREMENTS AND SCOPE",
                "This regulation establishes fundamental requirements and procedures for ensuring compliance with federal standards and promoting public safety and welfare. The regulation applies to all covered entities and activities within the scope of federal jurisdiction. Compliance with these requirements is mandatory and subject to federal oversight and enforcement.",
                
                "SECTION " + partNumber + ".2 IMPLEMENTATION PROCEDURES",
                "Covered entities must implement appropriate procedures and maintain comprehensive documentation to demonstrate compliance with applicable requirements. Implementation plans must be developed and submitted according to established schedules. Regular reviews and updates are required to ensure continued effectiveness and compliance with evolving standards.",
                
                "SECTION " + partNumber + ".3 OVERSIGHT AND ENFORCEMENT",
                "Federal agencies will conduct regular oversight activities including inspections, audits, and performance assessments to ensure regulatory compliance. Enforcement actions may include administrative orders, civil penalties, and other appropriate measures. Appeals and review processes are available for contested agency actions through established administrative procedures."
            };
        };
        
        // Randomly select 3-6 sections for variable content length
        int sectionsToInclude = 3 + random.nextInt(4); // 3-6 sections
        List<String> sectionList = Arrays.asList(sections);
        Collections.shuffle(sectionList, random);
        
        for (int i = 0; i < sectionsToInclude && i < sectionList.size(); i++) {
            content.append(sectionList.get(i)).append("\n\n");
            
            // Randomly add additional clauses to some sections (20% chance)
            if (random.nextDouble() < 0.2) {
                content.append(generateAdditionalClause(titleNumber)).append("\n\n");
            }
        }
        
        return content.toString().trim();
    }
    
    /**
     * Generate additional clauses for content variation
     */
    private String generateAdditionalClause(Integer titleNumber) {
        String[] clauses = switch (titleNumber) {
            case 7 -> new String[]{
                "Additional provisions may apply for organic certification requirements, including soil health standards, pest management protocols, and record-keeping obligations for certified organic operations.",
                "Seasonal considerations must be taken into account for compliance activities, particularly regarding harvest timing, storage conditions, and transportation schedules during peak production periods.",
                "Emergency procedures must be established for rapid response to food safety incidents, contamination events, or other circumstances that may compromise product integrity or public health."
            };
            case 10 -> new String[]{
                "Energy audit requirements may be waived for facilities demonstrating compliance through alternative verification methods approved by the appropriate regulatory authority.",
                "Advanced monitoring systems utilizing smart grid technology and real-time data collection may be substituted for traditional compliance reporting mechanisms where technically feasible.",
                "Renewable energy credits and carbon offset programs may be utilized to meet certain requirements under approved sustainability frameworks."
            };
            case 21 -> new String[]{
                "Clinical trial data requirements may include additional safety monitoring for vulnerable populations, enhanced adverse event reporting, and extended follow-up periods.",
                "Manufacturing quality standards must include validated sterilization procedures, contamination prevention protocols, and comprehensive batch testing requirements.",
                "Post-market surveillance activities must be conducted according to established risk-based monitoring protocols with regular safety updates."
            };
            case 40 -> new String[]{
                "Environmental impact assessments must consider cumulative effects, ecosystem interdependencies, and long-term sustainability implications of proposed regulatory actions.",
                "Community engagement requirements include public notice procedures, stakeholder consultation processes, and environmental justice considerations for affected populations.",
                "Remediation standards must incorporate current scientific understanding, technological feasibility, and cost-effectiveness analysis for cleanup activities."
            };
            default -> new String[]{
                "Compliance timelines may be adjusted based on operational complexity, resource availability, and technical implementation requirements for covered entities.",
                "Alternative compliance pathways may be available for organizations demonstrating equivalent protection through innovative approaches or technologies.",
                "Coordination requirements with other regulatory agencies must be addressed through established interagency procedures and information sharing protocols."
            };
        };
        
        return clauses[random.nextInt(clauses.length)];
    }
    
    private String getAgencyForTitle(Integer titleNumber) {
        return switch (titleNumber) {
            case 1 -> "General Services Administration";
            case 2 -> "Office of Management and Budget";
            case 3 -> "Executive Office of the President";
            case 4 -> "Government Accountability Office";
            case 5 -> "Office of Personnel Management";
            case 6 -> "Department of Homeland Security";
            case 7 -> "Department of Agriculture";
            case 8 -> "Department of Homeland Security";
            case 9 -> "Department of Agriculture";
            case 10 -> "Department of Energy";
            case 11 -> "Federal Election Commission";
            case 12 -> "Federal Reserve System";
            case 13 -> "Small Business Administration";
            case 14 -> "Federal Aviation Administration";
            case 15 -> "Department of Commerce";
            case 16 -> "Federal Trade Commission";
            case 17 -> "Securities and Exchange Commission";
            case 18 -> "Conservation of Power and Water Resources";
            case 19 -> "U.S. Customs and Border Protection";
            case 20 -> "Department of Labor";
            case 21 -> "Food and Drug Administration";
            case 22 -> "Department of State";
            case 23 -> "Department of Transportation";
            case 24 -> "Department of Housing and Urban Development";
            case 25 -> "Bureau of Indian Affairs";
            case 26 -> "Internal Revenue Service";
            case 27 -> "Bureau of Alcohol, Tobacco, Firearms and Explosives";
            case 28 -> "Department of Justice";
            case 29 -> "Department of Labor";
            case 30 -> "Department of the Interior";
            case 31 -> "Department of the Treasury";
            case 32 -> "Department of Defense";
            case 33 -> "U.S. Coast Guard";
            case 34 -> "Department of Education";
            case 35 -> "Reserved";
            case 36 -> "National Park Service";
            case 37 -> "U.S. Patent and Trademark Office";
            case 38 -> "Department of Veterans Affairs";
            case 39 -> "U.S. Postal Service";
            case 40 -> "Environmental Protection Agency";
            case 41 -> "General Services Administration";
            case 42 -> "Department of Health and Human Services";
            case 43 -> "Department of the Interior";
            case 44 -> "Federal Emergency Management Agency";
            case 45 -> "Department of Health and Human Services";
            case 46 -> "Federal Maritime Commission";
            case 47 -> "Federal Communications Commission";
            case 48 -> "General Services Administration";
            case 49 -> "Department of Transportation";
            case 50 -> "U.S. Fish and Wildlife Service";
            default -> "Federal Government";
        };
    }
    
    private String getTitleName(Integer titleNumber) {
        return switch (titleNumber) {
            case 1 -> "General Provisions";
            case 2 -> "Federal Financial Assistance";
            case 3 -> "The President";
            case 4 -> "Accounts";
            case 5 -> "Administrative Personnel";
            case 6 -> "Domestic Security";
            case 7 -> "Agriculture";
            case 8 -> "Aliens and Nationality";
            case 9 -> "Animals and Animal Products";
            case 10 -> "Energy";
            case 11 -> "Federal Elections";
            case 12 -> "Banks and Banking";
            case 13 -> "Business Credit and Assistance";
            case 14 -> "Aeronautics and Space";
            case 15 -> "Commerce and Foreign Trade";
            case 16 -> "Commercial Practices";
            case 17 -> "Commodity and Securities Exchanges";
            case 18 -> "Conservation of Power and Water Resources";
            case 19 -> "Customs Duties";
            case 20 -> "Employees' Benefits";
            case 21 -> "Food and Drugs";
            case 22 -> "Foreign Relations";
            case 23 -> "Highways";
            case 24 -> "Housing and Urban Development";
            case 25 -> "Indians";
            case 26 -> "Internal Revenue";
            case 27 -> "Alcohol, Tobacco Products and Firearms";
            case 28 -> "Judicial Administration";
            case 29 -> "Labor";
            case 30 -> "Mineral Resources";
            case 31 -> "Money and Finance: Treasury";
            case 32 -> "National Defense";
            case 33 -> "Navigation and Navigable Waters";
            case 34 -> "Education";
            case 35 -> "Reserved";
            case 36 -> "Parks, Forests, and Public Property";
            case 37 -> "Patents, Trademarks, and Copyrights";
            case 38 -> "Pensions, Bounties, and Veterans' Relief";
            case 39 -> "Postal Service";
            case 40 -> "Protection of Environment";
            case 41 -> "Public Contracts and Property Management";
            case 42 -> "Public Health";
            case 43 -> "Public Lands: Interior";
            case 44 -> "Emergency Management and Assistance";
            case 45 -> "Public Welfare";
            case 46 -> "Shipping";
            case 47 -> "Telecommunication";
            case 48 -> "Federal Acquisition Regulations System";
            case 49 -> "Transportation";
            case 50 -> "Wildlife and Fisheries";
            default -> "Federal Regulations";
        };
    }
    
    private Integer calculateWordCount(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 0;
        }
        return content.trim().split("\\s+").length;
    }
    
    private String generateChecksum(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "mock-checksum-" + System.currentTimeMillis();
        }
    }
    
    /**
     * Generate enhanced mock data with varied content lengths and potential relationships
     */
    public List<Regulation> generateEnhancedMockRegulations(Integer titleNumber, Integer count) {
        List<Regulation> mockRegulations = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            Regulation regulation = createVariedMockRegulation(titleNumber, String.valueOf(i));
            mockRegulations.add(regulation);
        }
        
        return mockRegulations;
    }
    
    /**
     * Create mock regulation with varied content length (200-500 words)
     */
    public Regulation createVariedMockRegulation(Integer titleNumber, String partNumber) {
        Regulation regulation = new Regulation();
        
        // Basic identifiers
        regulation.setCfrTitle(titleNumber);
        regulation.setPartNumber(partNumber);
        regulation.setAgencyName(getAgencyForTitle(titleNumber));
        
        // Generate varied content with different lengths
        String mockTitle = generateVariedMockTitle(titleNumber, partNumber);
        String mockContent = generateVariedMockContent(titleNumber, partNumber);
        
        regulation.setTitle(mockTitle);
        regulation.setContent(mockContent);
        regulation.setWordCount(calculateWordCount(mockContent));
        regulation.setContentChecksum(generateChecksum(mockContent));
        
        // Mock amendment dates with more variety
        LocalDate baseDate = LocalDate.of(2018, 1, 1);
        regulation.setLatestAmendedOn(baseDate.plusDays(random.nextInt(1000)));
        regulation.setLatestIssueDate(baseDate.plusDays(random.nextInt(800)));
        regulation.setLastUpdatedOn(baseDate.plusDays(random.nextInt(1200)));
        regulation.setAmendmentCount(random.nextInt(15) + 1);
        regulation.setEcfrLastModified(LocalDateTime.now().minusDays(random.nextInt(365)));
        regulation.setSourceUrl("https://ecfr.federalregister.gov/api/versioner/v1/structure/2025-09-18/title-" + titleNumber + ".json");
        
        return regulation;
    }
    
    /**
     * Generate varied mock titles with different regulatory focus areas
     */
    private String generateVariedMockTitle(Integer titleNumber, String partNumber) {
        String[] titleTypes = {
            "Standards", "Regulations", "Guidelines", "Requirements", "Procedures", 
            "Policies", "Rules", "Criteria", "Specifications", "Protocols"
        };
        
        String[] focusAreas = switch (titleNumber) {
            case 7 -> new String[]{"Conservation Program", "Agricultural Marketing Service", "Organic Certification", "Food Safety", "Rural Development", "Crop Insurance"};
            case 10 -> new String[]{"Energy Efficiency", "Nuclear Regulatory", "Environmental Impact", "Safety Standards", "Renewable Energy"};
            case 21 -> new String[]{"Clinical Trial", "Dietary Supplement", "Medical Device", "Tobacco Product", "Food Additive", "Drug Manufacturing"};
            case 40 -> new String[]{"Air Quality", "Water Protection", "Hazardous Waste", "Environmental Compliance", "Emission Control"};
            default -> new String[]{"Federal Compliance", "Administrative", "General Regulatory", "Public Safety", "Operational"};
        };
        
        String titleType = titleTypes[random.nextInt(titleTypes.length)];
        String focusArea = focusAreas[random.nextInt(focusAreas.length)];
        
        return focusArea + " " + titleType + " - Part " + partNumber;
    }
    
    /**
     * Generate varied mock content with different lengths and complexity
     */
    private String generateVariedMockContent(Integer titleNumber, String partNumber) {
        StringBuilder content = new StringBuilder();
        
        // Randomly select 3-6 sections instead of always 5
        int numSections = 3 + random.nextInt(4); // 3-6 sections
        String[] sectionTemplates = getVariedSectionTemplates(titleNumber, partNumber);
        
        // Randomly select sections to include
        List<Integer> selectedSections = new ArrayList<>();
        for (int i = 0; i < Math.min(numSections, sectionTemplates.length / 2); i++) {
            int sectionIndex = i * 2; // Section headers are at even indices
            selectedSections.add(sectionIndex);
        }
        
        for (int sectionIndex : selectedSections) {
            if (sectionIndex < sectionTemplates.length - 1) {
                // Add section header
                content.append(sectionTemplates[sectionIndex]).append("\n\n");
                
                // Add section content (at odd indices)
                String sectionContent = sectionTemplates[sectionIndex + 1];
                
                // Randomly vary content length
                if (random.nextBoolean()) {
                    // Add additional detail for some sections
                    sectionContent += generateAdditionalClause();
                }
                
                content.append(sectionContent).append("\n\n");
            }
        }
        
        // Sometimes add a final summary or implementation section
        if (random.nextDouble() < 0.4) { // 40% chance
            content.append("SECTION ").append(partNumber).append(".").append(numSections + 1)
                   .append(" IMPLEMENTATION AND COMPLIANCE\n\n");
            content.append("These regulations shall be implemented in accordance with established procedures. ")
                   .append("Compliance monitoring will be conducted through regular reviews and inspections. ")
                   .append("Non-compliance may result in enforcement actions as provided by law.");
        }
        
        return content.toString().trim();
    }
    
    /**
     * Generate additional regulatory clauses for content variation
     */
    private String generateAdditionalClause() {
        String[] additionalClauses = {
            " Entities subject to these requirements must maintain comprehensive documentation demonstrating ongoing compliance with all applicable provisions.",
            " Regular training programs must be established to ensure personnel understand their responsibilities under this regulation.",
            " Quality assurance measures must be implemented to verify consistent application of required procedures and standards.",
            " Notification requirements apply when certain conditions or thresholds are met, with specific timeframes for reporting.",
            " Exemptions may be available for certain circumstances, subject to approval and ongoing oversight by the appropriate regulatory authority.",
            " Periodic reviews of implementation effectiveness will be conducted to identify areas for improvement or regulatory updates.",
            " Coordination with other federal, state, and local requirements is essential to ensure comprehensive regulatory compliance.",
            " Record retention requirements specify minimum periods for maintaining documentation related to compliance activities.",
            " Public participation opportunities may be provided during certain regulatory processes or decision-making activities.",
            " Appeals processes are available for entities that disagree with regulatory determinations or enforcement actions."
        };
        
        return additionalClauses[random.nextInt(additionalClauses.length)];
    }
    
    /**
     * Get varied section templates with different content types
     */
    private String[] getVariedSectionTemplates(Integer titleNumber, String partNumber) {
        return switch (titleNumber) {
            case 7 -> getAgricultureVariedSections(partNumber);
            case 10 -> getEnergyVariedSections(partNumber);
            case 21 -> getFDAVariedSections(partNumber);
            case 40 -> getEPAVariedSections(partNumber);
            default -> getGeneralVariedSections(partNumber);
        };
    }
    
    private String[] getAgricultureVariedSections(String partNumber) {
        return new String[]{
            "SECTION " + partNumber + ".1 AGRICULTURAL PRODUCTION STANDARDS",
            "Agricultural production must meet established quality and safety standards. Producers shall implement good agricultural practices including proper soil management, integrated pest management, and water conservation measures. Documentation of production methods and inputs used must be maintained.",
            
            "SECTION " + partNumber + ".2 CERTIFICATION AND INSPECTION",
            "Annual certification is required for all agricultural operations. Inspections will verify compliance with production standards, record-keeping requirements, and facility conditions. Non-compliance may result in certification suspension.",
            
            "SECTION " + partNumber + ".3 MARKETING AND LABELING",
            "Agricultural products must be accurately labeled with origin, production methods, and relevant certifications. Marketing claims must be substantiated and comply with advertising standards.",
            
            "SECTION " + partNumber + ".4 FINANCIAL ASSISTANCE PROGRAMS",
            "Eligible producers may receive financial assistance for implementing conservation practices, improving infrastructure, or adopting sustainable production methods. Applications must be submitted by specified deadlines.",
            
            "SECTION " + partNumber + ".5 ENVIRONMENTAL COMPLIANCE",
            "Agricultural operations must comply with environmental protection requirements including water quality protection, soil conservation, and wildlife habitat preservation. Environmental management plans may be required.",
            
            "SECTION " + partNumber + ".6 RESEARCH AND DEVELOPMENT",
            "Support for agricultural research focuses on improving productivity, sustainability, and innovation. Research priorities include climate adaptation, new technologies, and market development."
        };
    }
    
    private String[] getEnergyVariedSections(String partNumber) {
        return new String[]{
            "SECTION " + partNumber + ".1 ENERGY EFFICIENCY REQUIREMENTS",
            "Energy efficiency standards apply to covered equipment and facilities. Minimum performance criteria must be met for new installations and existing equipment during major renovations.",
            
            "SECTION " + partNumber + ".2 RENEWABLE ENERGY INTEGRATION",
            "Facilities must evaluate opportunities for renewable energy integration. Implementation plans should consider solar, wind, geothermal, and other renewable technologies appropriate for the site.",
            
            "SECTION " + partNumber + ".3 MONITORING AND REPORTING",
            "Energy consumption data must be collected, analyzed, and reported annually. Monitoring systems should track usage patterns, identify efficiency opportunities, and verify compliance with performance targets.",
            
            "SECTION " + partNumber + ".4 TECHNOLOGY STANDARDS",
            "Equipment must meet established technology standards for efficiency, reliability, and safety. New technologies may be approved through the established evaluation and certification process."
        };
    }
    
    private String[] getFDAVariedSections(String partNumber) {
        return new String[]{
            "SECTION " + partNumber + ".1 PRODUCT SAFETY REQUIREMENTS",
            "All products must undergo safety evaluation before market authorization. Safety data must demonstrate acceptable risk-benefit profile for intended use populations.",
            
            "SECTION " + partNumber + ".2 MANUFACTURING QUALITY CONTROLS",
            "Manufacturing facilities must implement quality control systems ensuring product consistency and safety. Regular quality assessments and corrective actions are required.",
            
            "SECTION " + partNumber + ".3 CLINICAL TESTING PROTOCOLS",
            "Clinical studies must follow established protocols for patient safety, data integrity, and regulatory compliance. Institutional review board approval is required for human subjects research.",
            
            "SECTION " + partNumber + ".4 POST-MARKET SURVEILLANCE",
            "Post-market monitoring programs must track product performance and safety signals. Adverse events must be reported according to established timeframes and procedures."
        };
    }
    
    private String[] getEPAVariedSections(String partNumber) {
        return new String[]{
            "SECTION " + partNumber + ".1 ENVIRONMENTAL PROTECTION STANDARDS",
            "Environmental protection measures must prevent contamination of air, water, and soil resources. Emission limits and discharge standards apply to all covered activities.",
            
            "SECTION " + partNumber + ".2 PERMIT AND AUTHORIZATION REQUIREMENTS",
            "Environmental permits are required before commencing regulated activities. Permit applications must include detailed environmental impact assessments and mitigation measures.",
            
            "SECTION " + partNumber + ".3 MONITORING AND COMPLIANCE",
            "Environmental monitoring programs must verify compliance with permit conditions and regulatory standards. Monitoring data must be submitted according to established schedules.",
            
            "SECTION " + partNumber + ".4 REMEDIATION AND ENFORCEMENT",
            "Environmental violations require prompt remediation to prevent further harm. Enforcement actions may include penalties, corrective orders, or facility shutdowns for serious violations."
        };
    }
    
    private String[] getGeneralVariedSections(String partNumber) {
        return new String[]{
            "SECTION " + partNumber + ".1 GENERAL REQUIREMENTS",
            "This regulation establishes requirements for ensuring compliance with federal standards and promoting public safety. Covered entities must implement appropriate measures to meet regulatory objectives.",
            
            "SECTION " + partNumber + ".2 IMPLEMENTATION PROCEDURES",
            "Implementation must follow established procedures and timelines. Entities should develop internal procedures to ensure consistent application of regulatory requirements.",
            
            "SECTION " + partNumber + ".3 OVERSIGHT AND ENFORCEMENT",
            "Regular oversight activities verify compliance with regulatory requirements. Enforcement actions may be taken for violations, with penalties proportionate to the severity of non-compliance."
        };
    }
    
    /**
     * Generate mock regulatory relationships for testing relationship detection
     */
    public List<RegulationRelationship> generateMockRelationships(List<Regulation> regulations) {
        List<RegulationRelationship> relationships = new ArrayList<>();
        
        if (regulations.size() < 2) {
            return relationships;
        }
        
        // Generate some redundant relationships (high similarity)
        for (int i = 0; i < Math.min(3, regulations.size() - 1); i++) {
            if (random.nextDouble() < 0.3) { // 30% chance of redundancy
                RegulationRelationship relationship = new RegulationRelationship(
                    regulations.get(i).getId(), 
                    regulations.get(i + 1).getId(), 
                    RelationshipType.REDUNDANT, 
                    0.75 + random.nextDouble() * 0.2 // 0.75-0.95 similarity
                );
                relationship.setDetectedBy("MOCK_DATA_GENERATOR");
                relationship.setOverlapDetails("Mock redundant regulations with high content similarity for testing purposes.");
                relationships.add(relationship);
            }
        }
        
        // Generate some conflicting relationships
        for (int i = 0; i < Math.min(2, regulations.size() - 2); i++) {
            if (random.nextDouble() < 0.2) { // 20% chance of conflict
                RegulationRelationship relationship = new RegulationRelationship(
                    regulations.get(i).getId(), 
                    regulations.get(i + 2).getId(), 
                    RelationshipType.CONFLICTING, 
                    0.4 + random.nextDouble() * 0.3 // 0.4-0.7 similarity
                );
                
                ConflictSeverity[] severities = ConflictSeverity.values();
                relationship.setConflictSeverity(severities[random.nextInt(severities.length)]);
                relationship.setDetectedBy("MOCK_DATA_GENERATOR");
                relationship.setOverlapDetails("Mock conflicting regulations with overlapping scope but contradictory requirements.");
                relationships.add(relationship);
            }
        }
        
        // Generate some complementary relationships
        for (int i = 0; i < Math.min(2, regulations.size() - 1); i++) {
            if (random.nextDouble() < 0.4) { // 40% chance of complementary relationship
                RegulationRelationship relationship = new RegulationRelationship(
                    regulations.get(i).getId(), 
                    regulations.get((i + 1) % regulations.size()).getId(), 
                    RelationshipType.COMPLEMENTARY, 
                    0.3 + random.nextDouble() * 0.4 // 0.3-0.7 similarity
                );
                relationship.setDetectedBy("MOCK_DATA_GENERATOR");
                relationship.setOverlapDetails("Mock complementary regulations that work together to achieve regulatory objectives.");
                relationships.add(relationship);
            }
        }
        
        return relationships;
    }
}