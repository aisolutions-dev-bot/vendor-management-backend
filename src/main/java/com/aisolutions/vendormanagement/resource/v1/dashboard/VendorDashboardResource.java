package com.aisolutions.vendormanagement.resource.v1.dashboard;

import com.aisolutions.vendormanagement.dto.VendorDashboardDTO;
import com.aisolutions.vendormanagement.service.dashboard.VendorDashboardService;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Path("/api/v1/vendor/dashboard")
@Produces(MediaType.APPLICATION_JSON)
public class VendorDashboardResource {

  @Inject
  VendorDashboardService dashboardService;

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  /**
   * Get dashboard statistics for the logged-in vendor
   * 
   * @param fromDate Start date in yyyy-MM-dd format (defaults to Jan 1 of current
   *                 year)
   * @param toDate   End date in yyyy-MM-dd format (defaults to Dec 31 of current
   *                 year)
   * @return Dashboard statistics DTO
   */
  @GET
  @Path("/stats")
  public Uni<VendorDashboardDTO> getStats(
      @QueryParam("fromDate") String fromDate,
      @QueryParam("toDate") String toDate) {

    LocalDate from = parseDate(fromDate, getDefaultFromDate());
    LocalDate to = parseDate(toDate, getDefaultToDate());

    log.info("GET /api/v1/vendor/dashboard/stats - fromDate: {}, toDate: {}", from, to);

    return dashboardService.getStats(from, to);
  }

  private LocalDate parseDate(String dateStr, LocalDate defaultValue) {
    if (dateStr == null || dateStr.isBlank()) {
      return defaultValue;
    }
    try {
      return LocalDate.parse(dateStr, DATE_FORMATTER);
    } catch (DateTimeParseException e) {
      log.warn("Invalid date format: {}, using default: {}", dateStr, defaultValue);
      return defaultValue;
    }
  }

  private LocalDate getDefaultFromDate() {
    // Jan 1 of current year
    return LocalDate.of(LocalDate.now().getYear(), 1, 1);
  }

  private LocalDate getDefaultToDate() {
    // Dec 31 of current year
    return LocalDate.of(LocalDate.now().getYear(), 12, 31);
  }
}