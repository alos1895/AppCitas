<?php
/**
 * Controlador de doctores
 */

require_once "AuthorizationManager.php";

class doctors
{
    const AVAILABILITY_SEGMENT = "availability";

    const PARAM_DATE = "date";
    const PARAM_MEDICAL_CENTER = 'medical-center';
    const PARAM_TIME_SCHEDULE = 'time-schedule';

    const COL_DOCTOR_ID = 'id';

    const JSON_TIMES = 'times';
    const VALUE_MORNING = 'morning';
    const VALUE_EVENING = 'afternoon';

    public static function get($urlSegments)
    {
        AuthorizationManager::getInstance()->authorizeAffiliate();

        if (isset($urlSegments[0])
            && strcmp(self::AVAILABILITY_SEGMENT, $urlSegments[0]) == 0) {

            $queryParams = array();

            if (isset($_SERVER['QUERY_STRING'])) {
                parse_str($_SERVER['QUERY_STRING'], $queryParams);
            }

            if (!isset($queryParams[self::PARAM_MEDICAL_CENTER])
                || empty($queryParams[self::PARAM_MEDICAL_CENTER])
                || !isset($queryParams[self::PARAM_DATE])
                || empty($queryParams[self::PARAM_DATE])
                || !isset($queryParams[self::PARAM_TIME_SCHEDULE])
                || empty($queryParams[self::PARAM_TIME_SCHEDULE])) {
                throw new ApiException(
                    400,
                    0,
                    "Revise que los parámetros para fecha, centro médico y jornada estén especificados",
                    "http://localhost",
                    "Revise que estén definidos los parámetros date, medical-center y time-schedule"
                );
            }

            $medicalCenterId = $queryParams[self::PARAM_MEDICAL_CENTER];
            $date = $queryParams[self::PARAM_DATE];
            $timeSchedule = $queryParams[self::PARAM_TIME_SCHEDULE];

            $results = self::retrieveDoctorsSchedules($medicalCenterId, $date, $timeSchedule);

            return ["results" => $results];
        } else {
            throw new ApiException(
                400,
                0,
                "El recurso está mal referenciado",
                "http://localhost",
                "El recurso $_SERVER[REQUEST_URI] no esta sujeto a resultados"
            );
        }
    }

    public static function retrieveDoctorsSchedules($medicalCenterId, $date, $timeSchedule)
    {
        try {
            $pdo = MysqlManager::get()->getDb();
            $doctors = array();

            $query =
                "SELECT id, name, specialty, description
                  FROM doctor
                  WHERE exists(SELECT doctor_schedule.doctor_id
                  FROM doctor_schedule
                  WHERE doctor_id = doctor.id AND available = TRUE 
                  AND date = ? AND medical_center_id = ?)";

            $stm = $pdo->prepare($query);
            $stm->bindParam(1, $date);
            $stm->bindParam(2, $medicalCenterId, PDO::PARAM_INT);

            if ($stm->execute()) {

                while ($doctor = $stm->fetch(PDO::FETCH_ASSOC)) {
                    $doctorId = $doctor[self::COL_DOCTOR_ID];
                    $times = self::retrieveTimeSlots($doctorId, $date, $timeSchedule);

                    if (count($times) > 0) {
                        $doctor[self::JSON_TIMES] = $times;
                        array_push($doctors, $doctor);
                    }
                }

                return $doctors;

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

    private static function retrieveTimeSlots($doctorId, $date, $timeSchedule)
    {
        $pdo = MysqlManager::get()->getDb();

        $timeCondition = '';

        switch ($timeSchedule) {
            case self::VALUE_MORNING:
                $timeCondition = " AND start_time BETWEEN '06:00' AND '12:00'";
                break;
            case self::VALUE_EVENING:
                $timeCondition = " AND start_time BETWEEN '12:00' AND '18:00'";
                break;
            default:
                // TODO: Lanza una excepción para no procesar un valor diferente
        }

        $query = "SELECT a.start_time
                        FROM doctor
                        INNER JOIN doctor_schedule b
                            ON doctor.id = b.doctor_id
                        INNER JOIN attention_time_slot a
                            ON b.attention_time_slot_id = a.id
                        WHERE doctor.id = ? AND b.available = TRUE AND
                              b.date = ?";

        $query = $query . $timeCondition;

        $stm = $pdo->prepare($query);

        $stm->bindParam(1, $doctorId, PDO::PARAM_INT);
        $stm->bindParam(2, $date);
        $stm->execute();

        return $stm->fetchAll(PDO::FETCH_COLUMN);
    }
}