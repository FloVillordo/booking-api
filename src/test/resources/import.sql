
insert into public.calendar_availability(day) values (now()+ INTERVAL '2 day'), (now() + INTERVAL '3 day'), (now() + INTERVAL '8 day')

insert into public.booking(user_name, user_email, arrival_date, departure_date, status) values ('Pepito Juarez','pepito.juarez@pepito.com', now()+ INTERVAL '2 day', (now() + INTERVAL '4 day'),0)

insert into public.booking(user_name, user_email, arrival_date, departure_date, status) values ('Pepito Pepito','pepito@pepito.com', now()+ INTERVAL '8 day', (now() + INTERVAL '9 day'),0)
