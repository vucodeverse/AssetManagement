package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.response.DashboardDTO;
import edu.fpt.groupfive.dto.response.StaffDashboardDTO;

public interface DashboardService {
    DashboardDTO getDirectorDashboardData();

    StaffDashboardDTO getStaffDashboardData();
}
