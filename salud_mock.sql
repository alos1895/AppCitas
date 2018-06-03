-- phpMyAdmin SQL Dump
-- version 4.7.4
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 21-01-2018 a las 18:20:37
-- Versión del servidor: 10.1.29-MariaDB
-- Versión de PHP: 7.2.0

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `salud_mock`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `affiliate`
--

CREATE TABLE `affiliate` (
  `id` varchar(10) NOT NULL,
  `hash_password` varchar(256) NOT NULL,
  `name` varchar(128) NOT NULL,
  `address` varchar(128) NOT NULL,
  `gender` enum('F','M') NOT NULL,
  `token` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Volcado de datos para la tabla `affiliate`
--

INSERT INTO `affiliate` (`id`, `hash_password`, `name`, `address`, `gender`, `token`) VALUES
('1234567890', '$2y$10$n/m2o.IoosZCcCkZeL.vh.de.oGyzwAi5FwDp43JDMT5mqSpgELRS', 'Fernando', 'Calle 23 #2', 'M', '44945899e5c49b7ff8.48092936');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `appointment`
--

CREATE TABLE `appointment` (
  `id` int(8) NOT NULL,
  `date_and_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `service` varchar(64) NOT NULL,
  `status` enum('Activa','Cumplida','Cancelada') NOT NULL DEFAULT 'Activa',
  `affiliate_id` varchar(10) NOT NULL,
  `doctor_id` int(7) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Volcado de datos para la tabla `appointment`
--

INSERT INTO `appointment` (`id`, `date_and_time`, `service`, `status`, `affiliate_id`, `doctor_id`) VALUES
(3, '2017-02-23 17:25:31', 'Oftalmología', 'Cancelada', '1234567890', 1000010),
(4, '2017-02-23 17:25:32', 'Medicina General', 'Cancelada', '1234567890', 1000005),
(5, '2017-02-21 22:57:05', 'Cirugía General', 'Cancelada', '1234567890', 1000009),
(6, '2017-02-21 22:56:58', 'Urología', 'Cancelada', '1234567890', 1000010),
(7, '2017-02-21 21:03:16', 'Cirugía General', 'Cumplida', '1234567890', 1000004),
(8, '2017-02-23 17:25:27', 'Medicina General', 'Cancelada', '1234567890', 1000006),
(9, '2017-02-21 22:01:20', 'Urología', 'Cancelada', '1234567890', 1000004),
(10, '2017-02-23 17:25:33', 'Medicina General', 'Cancelada', '1234567890', 1000010),
(11, '2017-02-23 17:25:35', 'Medicina General', 'Cancelada', '1234567890', 1000008),
(12, '2017-02-23 17:25:36', 'Cirugía General', 'Cancelada', '1234567890', 1000008),
(13, '2017-02-21 22:13:34', 'Odontología', 'Cancelada', '1234567890', 1000006),
(14, '2017-02-21 21:03:34', 'Medicina General', 'Cumplida', '1234567890', 1000001);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `attention_time_slot`
--

CREATE TABLE `attention_time_slot` (
  `id` int(11) NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Volcado de datos para la tabla `attention_time_slot`
--

INSERT INTO `attention_time_slot` (`id`, `start_time`, `end_time`) VALUES
(1, '06:00:00', '06:30:00'),
(2, '06:30:00', '07:00:00'),
(3, '07:00:00', '07:30:00'),
(4, '07:30:00', '08:00:00'),
(5, '08:30:00', '09:00:00'),
(6, '09:00:00', '09:30:00'),
(7, '09:30:00', '10:00:00'),
(8, '10:30:00', '11:00:00'),
(9, '11:30:00', '12:00:00'),
(10, '12:30:00', '13:00:00'),
(11, '13:00:00', '13:30:00'),
(12, '13:30:00', '14:00:00'),
(13, '14:00:00', '14:30:00'),
(14, '14:30:00', '15:00:00'),
(15, '15:00:00', '15:30:00'),
(16, '15:30:00', '16:00:00');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `doctor`
--

CREATE TABLE `doctor` (
  `id` int(7) NOT NULL,
  `name` varchar(32) NOT NULL,
  `date_joined` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `specialty` varchar(64) DEFAULT NULL,
  `medical_center_id` int(5) DEFAULT NULL,
  `description` varchar(128) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Volcado de datos para la tabla `doctor`
--

INSERT INTO `doctor` (`id`, `name`, `date_joined`, `specialty`, `medical_center_id`, `description`) VALUES
(1000001, 'Mark Cooper', '2011-06-27 15:33:29', 'Anatomía Patológica', 10001, NULL),
(1000002, 'Carlos Simmons', '2012-11-17 12:19:43', 'Anestesiología y Recuperación', 10001, NULL),
(1000003, 'Andrea Harvey', '2014-03-03 18:41:37', 'Gastroenterología', 10005, NULL),
(1000004, 'Pamela Woods', '2014-10-01 21:45:37', 'Infectología Pediátrica', 10002, NULL),
(1000005, 'Heather Ray', '2012-06-12 00:57:16', 'Nefrología', 10003, NULL),
(1000006, 'Amanda James', '2012-08-15 09:08:47', 'Neurología', 10002, NULL),
(1000007, 'Jennifer Stevens', '2012-03-19 21:15:29', 'Oncología Quirúrgica', 10001, NULL),
(1000008, 'Willie Freeman', '2015-05-05 15:09:00', 'Pediatría', 10003, NULL),
(1000009, 'Norma Hunter', '2015-06-12 00:03:47', 'Urología', 10004, NULL),
(1000010, 'Bobby Martinez', '2013-01-19 17:02:36', 'Anatomía Patológica', 10003, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `doctor_schedule`
--

CREATE TABLE `doctor_schedule` (
  `doctor_id` int(11) NOT NULL,
  `attention_time_slot_id` int(11) NOT NULL,
  `available` tinyint(1) NOT NULL DEFAULT '1',
  `date` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `medical_center`
--

CREATE TABLE `medical_center` (
  `id` int(5) NOT NULL,
  `address` varchar(64) NOT NULL,
  `name` varchar(32) NOT NULL DEFAULT 'Por definir',
  `description` varchar(128) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Volcado de datos para la tabla `medical_center`
--

INSERT INTO `medical_center` (`id`, `address`, `name`, `description`) VALUES
(10001, '7533 Carey Park', 'Clínica Occidente', 'Mauris ullamcorper purus sit amet nulla.'),
(10002, '6 Nobel Park', 'Clínica Rostro y Figura', 'Integer non velit.'),
(10003, '2 Onsgard Hill', 'Centro Medico Salud para Todos', 'Aliquam augue quam, sollicitudin vitae, consectetuer eget, rutrum at, lorem.'),
(10004, '5 Pond Crossing', 'Hospital Carlos Carmona Montoya', 'Donec ut mauris eget massa tempor convallis.'),
(10005, '6 Waxwing Circle', 'Hospital San Juan de Dios', 'Nulla tellus.');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `affiliate`
--
ALTER TABLE `affiliate`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `appointment`
--
ALTER TABLE `appointment`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `appointment_date_and_time_uindex` (`date_and_time`),
  ADD KEY `affiliate_id` (`affiliate_id`),
  ADD KEY `doctor_id` (`doctor_id`);

--
-- Indices de la tabla `attention_time_slot`
--
ALTER TABLE `attention_time_slot`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `doctor`
--
ALTER TABLE `doctor`
  ADD PRIMARY KEY (`id`),
  ADD KEY `doctor_ibfk_1` (`medical_center_id`);

--
-- Indices de la tabla `doctor_schedule`
--
ALTER TABLE `doctor_schedule`
  ADD PRIMARY KEY (`doctor_id`,`attention_time_slot_id`,`date`),
  ADD KEY `doctor_schedule_schedule_id_fk` (`attention_time_slot_id`);

--
-- Indices de la tabla `medical_center`
--
ALTER TABLE `medical_center`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `appointment`
--
ALTER TABLE `appointment`
  MODIFY `id` int(8) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=37;

--
-- AUTO_INCREMENT de la tabla `attention_time_slot`
--
ALTER TABLE `attention_time_slot`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT de la tabla `doctor`
--
ALTER TABLE `doctor`
  MODIFY `id` int(7) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=1000011;

--
-- AUTO_INCREMENT de la tabla `medical_center`
--
ALTER TABLE `medical_center`
  MODIFY `id` int(5) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10006;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `appointment`
--
ALTER TABLE `appointment`
  ADD CONSTRAINT `appointment_ibfk_1` FOREIGN KEY (`affiliate_id`) REFERENCES `affiliate` (`id`),
  ADD CONSTRAINT `appointment_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`);

--
-- Filtros para la tabla `doctor`
--
ALTER TABLE `doctor`
  ADD CONSTRAINT `doctor_ibfk_1` FOREIGN KEY (`medical_center_id`) REFERENCES `medical_center` (`id`) ON DELETE NO ACTION;

--
-- Filtros para la tabla `doctor_schedule`
--
ALTER TABLE `doctor_schedule`
  ADD CONSTRAINT `doctor_schedule_doctor_id_fk` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`),
  ADD CONSTRAINT `doctor_schedule_schedule_id_fk` FOREIGN KEY (`attention_time_slot_id`) REFERENCES `attention_time_slot` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
