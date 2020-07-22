/*
CREATE TABLE public.calendar_availability
(
    id SERIAL NOT NULL,
    day date NOT NULL,
    available boolean NOT NULL default TRUE

);


DO $$
DECLARE
 	currentDate date := NOW();
	EndDate date := currentDate + 365;
BEGIN
While CurrentDate <= EndDate LOOP
   Insert Into public.calendar_availability(day) values (CurrentDate);
   CurrentDate :=  currentDate + 1;
 END LOOP;
End$$;
*/
