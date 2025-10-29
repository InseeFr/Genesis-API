package fr.insee.genesis.controller.rest;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

@ApiResponse(responseCode = "400", description = "Invalid request parameters")
@ApiResponse(responseCode = "401", description = "Unauthorized")
@ApiResponse(responseCode = "403", description = "Forbidden")
@ApiResponse(responseCode = "404", description = "Requested resource not found")
@ApiResponse(responseCode = "500", description = "Internal server error")
public interface CommonApiResponse {
}
