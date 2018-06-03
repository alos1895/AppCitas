<?php

require 'controllers/affiliates.php';
require 'controllers/appointments.php';
require 'controllers/medical_centers.php';
require 'controllers/doctors.php';

require 'views/XmlView.php';
require 'views/JsonView.php';
require 'utils/ApiException.php';

// Apagado/Encendido de errores PHP
if (strtolower(getenv('DEVELOPMENT')) == "on") {
    ini_set('display_startup_errors', 1);
    ini_set('display_errors', 1);
    error_reporting(E_ALL);
}

// Obtener valor del parámetro 'format' para el formato de la respuesta
$format = isset($_GET['format']) ? $_GET['format'] : 'json';

// Crear representación de la vista para el formato elegido
if (strcasecmp($format, 'xml') == 0) {
    $apiView = new XmlView();
} else {
    $apiView = new JsonView();
}

// Registrar manejador de excepciones
set_exception_handler(function ($exception) use ($apiView) {
    if ($exception instanceof ApiException) {
        http_response_code($exception->getStatus());
        $apiView->render($exception->toArray());
    } else {
        $apiException = new ApiException(500, 0, $exception->getMessage(),
            "", $exception->getTraceAsString());
        http_response_code($apiException->getStatus());
        $apiView->render($apiException->toArray());
    }
});

// Extraer segmento de la url
if (isset($_GET['PATH_INFO'])) {
    $urlSegments = explode('/', $_GET['PATH_INFO']);
} else {
    throw new ApiException(
        404,
        0,
        "El recurso al que intentas acceder no existe",
        "http://localhost",
        "No existe un resource definido en: http://$_SERVER[HTTP_HOST]$_SERVER[REQUEST_URI]");
}

// Obtener recurso proveniente de la URL
$resource = array_shift($urlSegments);
$apiResources = array('affiliates', 'appointments', 'medical-centers', 'doctors');

// Comprobar si existe el recurso
if (!in_array($resource, $apiResources)) {
    throw new ApiException(
        404,
        0,
        "El recurso al que intentas acceder no existe",
        "http://localhost",
        "No existe un resource definido en: http://$_SERVER[HTTP_HOST]$_SERVER[REQUEST_URI]");
}

// Transformar método HTTP a minúsculas
$httpMethod = strtolower($_SERVER['REQUEST_METHOD']);

// Formatear recurso para que coincida con el controlador
$resource = str_replace('-', '_', $resource);

// Determinar acción según el método HTTP
switch ($httpMethod) {
    case 'get':
    case 'post':
    case 'put':
    case 'patch':
    case 'delete':
        if (method_exists($resource, $httpMethod)) {
            $apiResponse = call_user_func(array($resource, $httpMethod), $urlSegments);
            $apiView->render($apiResponse);
            break;
        }
    default:

        // Método no permitido sobre el recurso
        $methodNotAllowed = new ApiException(
            405,
            0,
            "Acción no permitida",
            "http://localhost",
            "No se puede aplicar el método $_SERVER[REQUEST_METHOD] sobre el recurso \"$resource\"");
        $apiView->render($methodNotAllowed->toArray());

}


