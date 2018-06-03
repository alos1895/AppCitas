<?php
/**
 * Controlador de centros medicos
 */
require_once 'AuthorizationManager.php';

class medical_centers
{
    public static function get($urlSegments){

        $affiliateId = AuthorizationManager::getInstance()->authorizeAffiliate();

        if (isset($urlSegments[1])) {
            throw new ApiException(
                400,
                0,
                "El recurso está mal referenciado",
                "http://localhost",
                "El recurso $_SERVER[REQUEST_URI] no esta sujeto a resultados"
            );
        }

        $medicalCenters= self::retrieveMedicalCenters();

        return ["results"=>$medicalCenters];
    }

    private static function retrieveMedicalCenters(){
        try {
            $pdo = MysqlManager::get()->getDb();

            $query = "SELECT * FROM medical_center";

            $preStm = $pdo->prepare($query);

            if ($preStm->execute()) {
                return $preStm->fetchAll(PDO::FETCH_ASSOC);
            } else {
                throw new ApiException(
                    500,
                    0,
                    "Error de base de datos en el servidor",
                    "http://localhost",
                    "Hubo un error ejecutando una sentencia SQL en la base de datos. Detalles:" . $pdo->errorInfo()[2]
                );
            }

        } catch (PDOException $e) {
            throw new ApiException(
                500,
                0,
                "Error de base de datos en el servidor",
                "http://localhost",
                "Ocurrió el siguiente error al consultar las citas médicas: " . $e->getMessage());
        }
    }
}