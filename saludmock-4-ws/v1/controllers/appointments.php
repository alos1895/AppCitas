<?php

/**
 * Controlador del endpoint /appointments
 */
require_once "AuthorizationManager.php";


class appointments
{
    const JSON_DATE_TIME = 'datetime';
    const JSON_SERVICE = 'service';
    const JSON_AFFILIATE = 'affiliate';
    const JSON_DOCTOR = 'doctor';

    public static function get($urlSegments)
    {
        // 1. Comprobar autorización del afiliado
        $affiliateId = AuthorizationManager::getInstance()->authorizeAffiliate();

        // 2. Verificaciones, restricciones, defensas
        if (isset($urlSegments[1])) {
            throw new ApiException(
                400,
                0,
                "El recurso está mal referenciado",
                "http://localhost",
                "El recurso $_SERVER[REQUEST_URI] no esta sujeto a resultados"
            );
        }

        $parameters = array();

        // 3. Obtener parámetros de la URL
        if (isset($_SERVER['QUERY_STRING'])) {
            parse_str($_SERVER['QUERY_STRING'], $parameters);
        }

        // 4. Invocar a la fuente de datos para retorno de citas médicas
        $appointments = self::retrieveAppointments($affiliateId, $parameters);
        return ["results" => $appointments];
    }

    public static function post($urlSegments)
    {
        $affiliateId = AuthorizationManager::getInstance()->authorizeAffiliate();

        $requestBody = file_get_contents('php://input');

        $newAppointment = json_decode($requestBody, true);

        if (self::saveAppointment($affiliateId, $newAppointment)) {
            return ["status" => 201, "message" => "Cita creada"];
        } else {
            throw new ApiException(
                500,
                0,
                "Error del servidor",
                "http://localhost",
                "Error en la base de datos del servidor");
        }
    }

    public static function put($urlSegments)
    {

    }

    public static function patch($urlSegments)
    {
        // Comprobar si el afiliado está autorizado
        $affiliateId = AuthorizationManager::getInstance()->authorizeAffiliate();

        // Extraer id de la cita
        if (!isset($urlSegments[0]) || empty($urlSegments[0])) {
            throw new ApiException(
                400,
                0,
                "Se requiere id de la cita",
                "http://localhost",
                "La URL debe tener la forma /appointments/:id para aplicar el método PATCH"
            );
        }
        $id = $urlSegments[0];

        // Verificar anomalías de la URL
        if (isset($urlSegments[1])) {
            throw new ApiException(
                400,
                0,
                "El recurso está mal referenciado",
                "http://localhost",
                "La URL no es de la forma /appointments/:id"
            );
        }

        // Extraer cuerpo de la petición
        $body = file_get_contents("php://input");

        $content_type = '';

        if (isset($_SERVER['CONTENT_TYPE'])) {
            $content_type = $_SERVER['CONTENT_TYPE'];
        }
        switch ($content_type) {
            case "application/json":
                $body_params = json_decode($body);
                if ($body_params) {
                    foreach ($body_params as $param_name => $param_value) {
                        $parameters[$param_name] = $param_value;
                    }
                }
                break;
            default:
                throw new ApiException(
                    400,
                    0,
                    "Formato de los datos no soportado",
                    "http://localhost",
                    "El cuerpo de la petición no usa el tipo application/json"
                );
        }

        if (empty($parameters)) {
            throw new ApiException(
                400,
                0,
                "No se especificaron atributos a modificar en la cita",
                "http://localhost",
                "El array de parámetros llegó vacío"
            );
        }

        // Modificar cita médica en la base de datos local
        $result = self::modifyAppointment($parameters, $id, $affiliateId);

        // Retornar mensaje de modificación
        if ($result > 0) {
            return ["status" => 200, "message" => "Cita médica modificada"];
        } else {
            throw new ApiException(
                409,
                0,
                "Hubo un conflicto al intentar modificar la cita",
                "http://localhost",
                "La modificación no afecta ninguna fila"
            );
        }
    }

    public static function delete($urlSegments)
    {

    }

    private static function retrieveAppointments($affiliateId, $parameters)
    {

        try {
            $pdo = MysqlManager::get()->getDb();

            /* status:
            ¿Viene como parámetro en la URL y su valor no es vacío?
            ¿No está definido o su valor es "Todas"?
                SI: Usar un espacio para consultar todas las citas
                NO: Formar condición para el WHERE con la columna "status"
            */
            $isStatusDefined = isset($parameters["status"]) || !empty($parameters["status"]);
            $isAllStatus = !$isStatusDefined || strcasecmp($parameters["status"], "Todas") == 0;
            $statusSqlString = $isAllStatus ? "" : " AND status = ?";

            // display: ¿Viene como parámetro en la URL y su valor no es vacío?
            $isDisplayDefined = isset($parameters["display"]) && !empty($parameters["display"]);

            $sentence = "SELECT * FROM appointment WHERE affiliate_id = ? " . $statusSqlString;

            if ($isDisplayDefined && $parameters["display"] == "list") {
                $sentence =
                    "SELECT a.id, a.date_and_time, a.service, a.status, a.affiliate_id," .
                    " b.name as doctor, c.name as medical_center " .
                    "FROM appointment as a INNER JOIN doctor as b ON a.doctor_id = b.id" .
                    " INNER JOIN medical_center as c ON b.medical_center_id = c.id " .
                    "WHERE affiliate_id = ?" . $statusSqlString;
                $sentence .= " order by a.date_and_time desc"; // Ordenamiento por fecha
            }


            // Preparar sentencia
            $preparedStatement = $pdo->prepare($sentence);
            $preparedStatement->bindParam(1, $affiliateId);
            if (!$isAllStatus) {
                $preparedStatement->bindParam(2, $parameters["status"]);
            }

            // Ejecutar sentencia
            if ($preparedStatement->execute()) {

                return $preparedStatement->fetchAll(PDO::FETCH_ASSOC);

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

    private static function modifyAppointment($parameters, $id, $affiliateId)
    {
        try {
            $pdo = MysqlManager::get()->getDb();

            // Concatenar expresiones para SET
            foreach ($parameters as $key => $value) {
                $compoundSet[] = $key . "=?";
            }

            // Componer sentencia UPDATE
            $sentence = "UPDATE appointment " .
                "SET " . implode(',', $compoundSet) .
                " WHERE id = ? AND affiliate_id = ?";

            // Preparar sentencia
            $preparedStatement = $pdo->prepare($sentence);

            $i = 1;
            foreach ($parameters as $value) {
                $preparedStatement->bindParam($i, $value);
                $i++;
            }
            $preparedStatement->bindParam($i, $id);
            $preparedStatement->bindParam($i + 1, $affiliateId);

            // Ejecutar sentencia
            if ($preparedStatement->execute()) {

                $rowCount = $preparedStatement->rowCount();
                return $rowCount;

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
                "Ocurrió el siguiente error al modificar la cita: " . $e->getMessage());
        }
    }

    private static function saveAppointment($affiliateId, $newAppointment)
    {
        $pdo = MysqlManager::get()->getDb();

        try {
            $pdo->beginTransaction();

            $op = "INSERT INTO appointment (date_and_time, service, affiliate_id, doctor_id) 
                  VALUES (?, ?, ?, ?)";


            $stm = $pdo->prepare($op);
            $stm->bindParam(1, $dateTime);
            $stm->bindParam(2, $service);
            $stm->bindParam(3, $affiliateId);
            $stm->bindParam(4, $doctorId);

            $dateTime = $newAppointment[self::JSON_DATE_TIME];
            $service = $newAppointment[self::JSON_SERVICE];
            $doctorId = $newAppointment[self::JSON_DOCTOR];

            $stm->execute();

            $explodeDateTime = explode(" ", $dateTime);
            $date = $explodeDateTime[0];
            $startTime = $explodeDateTime[1];

            self::markScheduleNotAvailable($doctorId, $startTime, $date);

            return $pdo->commit();

        } catch (PDOException $e) {
            $pdo->rollBack();

            throw new ApiException(
                500,
                0,
                "Error de base de datos en el servidor",
                "http://localhost",
                "Hubo un error ejecutando una sentencia SQL en la base de datos. Detalles:"
                . $e->getMessage()
            );
        }
    }

    private static function markScheduleNotAvailable($doctorId, $startTime, $date)
    {
        $pdo = MysqlManager::get()->getDb();

        $cmd = "UPDATE doctor_schedule
                SET available = FALSE
                WHERE doctor_id = ? AND attention_time_slot_id = 
                (SELECT id FROM attention_time_slot WHERE start_time = ?)
                 AND date = ? ";

        $stm = $pdo->prepare($cmd);
        $stm->bindParam(1, $doctorId);
        $stm->bindParam(2, $startTime);
        $stm->bindParam(3, $date);

        return $stm->execute();
    }
}