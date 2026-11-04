package com.frescuratotal.erp.inventory.model;

import com.frescuratotal.erp.common.DatabaseConnection;

import java.sql.*;

public class QualityDaoImpl implements QualityDao {

    @Override
    public void saveQuarantine(QuarantineRecord quarantine) {
        String sql = "INSERT INTO registro_cuarentena (id_lote, motivo, usuario) VALUES (?, ?, ?)";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try(PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, quarantine.getLotId());
            ps.setString(2, quarantine.getReason());
            ps.setString(3, quarantine.getRegisteredBy());

            ps.executeUpdate();
        }catch(SQLException e){
            throw new RuntimeException("Error saving quarantine for lot " + quarantine.getLotId());
        }
    }


    @Override
    public void resolveQuarantine(int lotId, String notes) {
        String sql = """
                UPDATE registro_cuarentena
                SET resuelta = TRUE, fecha_resolucion = NOW(), notas_resolucion = ?
                WHERE id_lote = ? AND resuelta = FALSE
                """;

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try(PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1,notes);
            ps.setInt(2,lotId);

            ps.executeUpdate();

        }catch(SQLException e){
            throw new RuntimeException("Error resolving quarantine for lot " + lotId,e);
        }
    }

    @Override
    public int saveInspection(QualityInspection inspection) {
        String sql = """
                INSERT INTO inspecciones_calidad (id_lote, resultado, nombre_inspector, motivo_rechazo, notas)
                VALUES (?, ?, ?, ?, ?)
                """;

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql,  Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, inspection.getLotId());
            ps.setString(2,inspection.getResult().getDbValue());
            ps.setString(3,inspection.getInspectorName());
            ps.setString(4, inspection.getRejectionReason());
            ps.setString(5, inspection.getNotes());

            ps.executeUpdate();
            try(ResultSet keys = ps.getGeneratedKeys()) {
                if(keys.next()) {
                    int id = keys.getInt(1);
                    inspection.setId(id);
                    return id;
                }
            }
        }catch(SQLException e){
            throw new RuntimeException("Error saving inspection for lot " + inspection.getLotId(), e);
        }
        throw new RuntimeException("No id returned for inspection");
    }

    @Override
    public void saveWaste(WasteRecord waste) {
        String sql = """
            INSERT INTO registro_mermas (id_lote, id_inspeccion, cantidad_perdida, motivo, usuario_registro)
            VALUES (?, ?, ?, ?, ?)
            """;

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try(PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, waste.getLotId());
            if(waste.getInspectionId() != null) {
                ps.setInt(2, waste.getInspectionId());
            } else{
                ps.setNull(2, Types.INTEGER);
            }
            ps.setBigDecimal(3,waste.getLostQuantity());
            ps.setString(4,waste.getReason());
            ps.setString(5,waste.getRegisteredBy());

            ps.executeUpdate();
        }catch (SQLException e){
            throw new RuntimeException("Error saving waste for lot " + waste.getLotId(), e);
        }
    }
}
