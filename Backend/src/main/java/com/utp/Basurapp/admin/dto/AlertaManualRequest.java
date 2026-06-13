package com.utp.Basurapp.admin.dto;

import lombok.Data;

@Data
public class AlertaManualRequest {
    private String mensaje;
    private double radioMetros;
    private String idCamion;
}
