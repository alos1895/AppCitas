<?php
/**
 * Manejador de autorizaciones sobre recursos
 */

class AuthorizationManager
{
    private static $authManager = null;

    /**
     * AuthorizationManager constructor.
     */
    final private function __construct()
    {
    }

    public static function getInstance(){
        if(self::$authManager==null){
            self::$authManager = new self();
        }
        return self::$authManager;
    }

    final protected function __clone() {
    }

    public function authorizeAffiliate()
    {

        $authHeaderValue = apache_request_headers()['Authorization'];
        if (!isset($authHeaderValue)) {
            throw new ApiException(
                401,
                0,
                "No está autorizado para acceder a este recurso",
                "http://localhost",
                "No viene el token en la cabecera de autorización"
            );
        }

        // Consultar base de datos por afiliado
        $affiliateId = self::isAffiliateAuthorized($authHeaderValue);

        if (empty($affiliateId)) {
            throw new ApiException(
                401,
                0,
                "No está autorizado para acceder a este recurso",
                "http://localhost",
                "No hay coincidencias del token del afiliado en la base de datos"
            );
        }
        return $affiliateId;
    }

    private function isAffiliateAuthorized($token)
    {
        if (empty($token)) {
            throw new ApiException(
                405,
                0,
                "No está autorizado para acceder a este recurso",
                "http://localhost",
                "La cabecera HTTP Authorization está vacía"
            );
        }

        try {
            $pdo = MysqlManager::get()->getDb();

            // Componer sentencia SELECT
            $sentence = "SELECT id FROM affiliate WHERE token = ?";

            // Preparar sentencia
            $preStatement = $pdo->prepare($sentence);
            $preStatement->bindParam(1, $token);

            // Ejecutar sentencia
            if ($preStatement->execute()) {
                // Retornar id del afiliado autorizado
                $result = $preStatement->fetchColumn();
                return $result;

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
                "Ocurrió el siguiente error al intentar insertar el afiliado: " . $e->getMessage());
        }

    }

}