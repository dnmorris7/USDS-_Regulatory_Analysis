package com.usds.regulations.service;

import com.opencsv.CSVWriter;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

@Service
public class CSVExportService {

    public String exportCFRTitlesToCSV() {
        StringWriter stringWriter = new StringWriter();
        
        try (CSVWriter csvWriter = new CSVWriter(stringWriter)) {
            // Write header
            String[] header = {"Title #", "Title Name", "Agency", "Regulations", "Status", "Last Updated"};
            csvWriter.writeNext(header);
            
            // Write mock data rows (replace with actual database query when available)
            List<String[]> mockData = getMockCFRData();
            for (String[] row : mockData) {
                csvWriter.writeNext(row);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generating CSV export", e);
        }
        
        return stringWriter.toString();
    }

    private List<String[]> getMockCFRData() {
        return Arrays.asList(
            new String[]{"1", "General Provisions", "General Services Administration", "34", "3 CONFLICTS", "2025-09-26"},
            new String[]{"2", "Grants and Agreements", "Office of Management and Budget", "12", "NO CONFLICTS", "2025-09-26"},
            new String[]{"3", "The President", "Executive Office of the President", "53", "5 CONFLICTS", "2025-09-26"},
            new String[]{"4", "Accounts", "Government Accountability Office", "28", "2 CONFLICTS", "2025-09-26"},
            new String[]{"5", "Administrative Personnel", "Office of Personnel Management", "45", "NO CONFLICTS", "2025-09-26"},
            new String[]{"6", "Domestic Security", "Department of Homeland Security", "38", "1 CONFLICTS", "2025-09-26"},
            new String[]{"7", "Agriculture", "Department of Agriculture", "167", "NO CONFLICTS", "2025-09-25"},
            new String[]{"8", "Aliens and Nationality", "Department of Homeland Security", "89", "4 CONFLICTS", "2025-09-25"},
            new String[]{"9", "Animals and Animal Products", "Department of Agriculture", "92", "NO CONFLICTS", "2025-09-25"},
            new String[]{"10", "Energy", "Department of Energy", "156", "7 CONFLICTS", "2025-09-24"},
            new String[]{"11", "Federal Elections", "Federal Election Commission", "23", "NO CONFLICTS", "2025-09-24"},
            new String[]{"12", "Banks and Banking", "Federal Reserve System", "78", "2 CONFLICTS", "2025-09-23"},
            new String[]{"13", "Business Credit and Assistance", "Small Business Administration", "45", "NO CONFLICTS", "2025-09-23"},
            new String[]{"14", "Aeronautics and Space", "Federal Aviation Administration", "134", "6 CONFLICTS", "2025-09-22"},
            new String[]{"15", "Commerce and Foreign Trade", "Department of Commerce", "98", "3 CONFLICTS", "2025-09-22"},
            new String[]{"16", "Commercial Practices", "Federal Trade Commission", "67", "NO CONFLICTS", "2025-09-21"},
            new String[]{"17", "Commodity and Securities Exchanges", "Securities and Exchange Commission", "89", "4 CONFLICTS", "2025-09-21"},
            new String[]{"18", "Conservation of Power and Water Resources", "Federal Energy Regulatory Commission", "112", "NO CONFLICTS", "2025-09-20"},
            new String[]{"19", "Customs Duties", "U.S. Customs and Border Protection", "56", "1 CONFLICTS", "2025-09-20"},
            new String[]{"20", "Employees' Benefits", "Department of Labor", "87", "NO CONFLICTS", "2025-09-19"},
            new String[]{"21", "Food and Drugs", "Food and Drug Administration", "203", "8 CONFLICTS", "2025-09-19"},
            new String[]{"22", "Foreign Relations", "Department of State", "43", "NO CONFLICTS", "2025-09-18"},
            new String[]{"23", "Highways", "Federal Highway Administration", "78", "2 CONFLICTS", "2025-09-18"},
            new String[]{"24", "Housing and Urban Development", "Department of Housing and Urban Development", "124", "5 CONFLICTS", "2025-09-17"},
            new String[]{"25", "Indians", "Bureau of Indian Affairs", "67", "NO CONFLICTS", "2025-09-17"},
            new String[]{"26", "Internal Revenue", "Internal Revenue Service", "189", "9 CONFLICTS", "2025-09-16"},
            new String[]{"27", "Alcohol, Tobacco Products and Firearms", "Bureau of Alcohol, Tobacco, Firearms and Explosives", "45", "NO CONFLICTS", "2025-09-16"},
            new String[]{"28", "Judicial Administration", "Department of Justice", "73", "3 CONFLICTS", "2025-09-15"},
            new String[]{"29", "Labor", "Department of Labor", "156", "NO CONFLICTS", "2025-09-15"},
            new String[]{"30", "Mineral Resources", "Department of the Interior", "98", "4 CONFLICTS", "2025-09-14"},
            new String[]{"31", "Money and Finance: Treasury", "Department of the Treasury", "112", "NO CONFLICTS", "2025-09-14"},
            new String[]{"32", "National Defense", "Department of Defense", "234", "12 CONFLICTS", "2025-09-13"},
            new String[]{"33", "Navigation and Navigable Waters", "U.S. Coast Guard", "89", "NO CONFLICTS", "2025-09-13"},
            new String[]{"34", "Education", "Department of Education", "167", "6 CONFLICTS", "2025-09-12"},
            new String[]{"35", "Panama Canal", "Panama Canal Commission", "12", "NO CONFLICTS", "2025-09-12"},
            new String[]{"36", "Parks, Forests, and Public Property", "National Park Service", "145", "NO CONFLICTS", "2025-09-11"},
            new String[]{"37", "Patents, Trademarks, and Copyrights", "U.S. Patent and Trademark Office", "56", "2 CONFLICTS", "2025-09-11"},
            new String[]{"38", "Pensions, Bonuses, and Veterans' Relief", "Department of Veterans Affairs", "189", "7 CONFLICTS", "2025-09-10"},
            new String[]{"39", "Postal Service", "U.S. Postal Service", "78", "NO CONFLICTS", "2025-09-10"},
            new String[]{"40", "Protection of Environment", "Environmental Protection Agency", "298", "15 CONFLICTS", "2025-09-09"},
            new String[]{"41", "Public Contracts and Property Management", "General Services Administration", "134", "NO CONFLICTS", "2025-09-09"},
            new String[]{"42", "Public Health", "Department of Health and Human Services", "267", "11 CONFLICTS", "2025-09-08"},
            new String[]{"43", "Public Lands: Interior", "Bureau of Land Management", "178", "NO CONFLICTS", "2025-09-08"},
            new String[]{"44", "Emergency Management and Assistance", "Federal Emergency Management Agency", "89", "3 CONFLICTS", "2025-09-07"},
            new String[]{"45", "Public Welfare", "Department of Health and Human Services", "201", "NO CONFLICTS", "2025-09-07"},
            new String[]{"46", "Shipping", "Maritime Administration", "67", "1 CONFLICTS", "2025-09-06"},
            new String[]{"47", "Telecommunication", "Federal Communications Commission", "156", "8 CONFLICTS", "2025-09-06"},
            new String[]{"48", "Federal Acquisition Regulations System", "General Services Administration", "234", "NO CONFLICTS", "2025-09-05"},
            new String[]{"49", "Transportation", "Department of Transportation", "298", "13 CONFLICTS", "2025-09-05"},
            new String[]{"50", "Wildlife and Fisheries", "U.S. Fish and Wildlife Service", "145", "NO CONFLICTS", "2025-09-04"}
        );
    }
}