
    alter table building 
       drop 
       foreign key FK5vart3g8xv4gkgagwxxwyiuqi;

    alter table building 
       drop 
       foreign key FKbp0gn3eiexsa5p6s20md9yfi7;

    alter table construction 
       drop 
       foreign key FKlkteuncyf95jg9hhq28yefrcl;

    alter table construction 
       drop 
       foreign key FKg139setxu2ng9hj6h7sgpyb9s;

    alter table fleet 
       drop 
       foreign key FKh6yguwrqsu1kah359o77c1b8h;

    alter table fleet 
       drop 
       foreign key FK2vpu4blpguup7j52xnn42ypnl;

    alter table fleet 
       drop 
       foreign key FKjo66qwgl0a9bba5x7xq23fvok;

    alter table fleet 
       drop 
       foreign key FKckq55cmimjpois3mst803atuy;

    alter table fleetcomposition 
       drop 
       foreign key FK2xo81l4vrqmcwboo06cumtens;

    alter table fleetcomposition 
       drop 
       foreign key FK8xjjuy4dvxqwloaaf4wge42qw;

    alter table hull 
       drop 
       foreign key FK65udyybp7syxvga5evxn8olhc;

    alter table hull 
       drop 
       foreign key FK4hpf1pawl0wynjx9kdg74opea;

    alter table job 
       drop 
       foreign key FK7otfjvk4vhy0gt0m3hnyam6au;

    alter table job 
       drop 
       foreign key FKdno72guom99osq9f36eixsd87;

    alter table job 
       drop 
       foreign key FKgbuspxcwyu67ktf3pkxwaxj6b;

    alter table job 
       drop 
       foreign key FK4ewa76co5drr08nptgdmax8d6;

    alter table job 
       drop 
       foreign key FK3urqlpl2jmbxlfk4q88i9i5tb;

    alter table module 
       drop 
       foreign key FKqxpwocsv3vwcws3g1yj7hpw8i;

    alter table module 
       drop 
       foreign key FK52hbj88ddt0mvoq1jv1rf5vk1;

    alter table moduleComposition 
       drop 
       foreign key FKgtipiaku2mvi9j3of7ju7th6g;

    alter table moduleComposition 
       drop 
       foreign key FKr2iuudhohjx8cacih40d1bpv6;

    alter table move 
       drop 
       foreign key FKg65nht3m74odamnrqiv1cdyl6;

    alter table move 
       drop 
       foreign key FKm0l3o2yx8pq8hu2bww8maoa98;

    alter table move 
       drop 
       foreign key FKa1bs79m293x3ok5ose0jli0r9;

    alter table move 
       drop 
       foreign key FK66wwxap7hrv54faje90tmrbb0;

    alter table move 
       drop 
       foreign key FKfhqgwhapcw4i2ydno4u1qlq77;

    alter table move 
       drop 
       foreign key FKr8obp03f86v1f41icg4xro1rl;

    alter table planet 
       drop 
       foreign key FKobjb6jgxji3jrrgoxy9r30uyc;

    alter table planet 
       drop 
       foreign key FK9cd80e9yxwnobejr9twlcknab;

    alter table planet 
       drop 
       foreign key FKjw116v1g0p9ghu41k1jddkw50;

    alter table planet 
       drop 
       foreign key FK2qd4p5ry3gaskjau8i2gutj0n;

    alter table research 
       drop 
       foreign key FKni50te130dndarqgicsq3svhb;

    alter table research 
       drop 
       foreign key FKch37eb44iv0ls442yu7usvvtp;

    alter table resources 
       drop 
       foreign key FK8l4tmivydxr3qd5g2hmes0ieh;

    alter table shipClass 
       drop 
       foreign key FK5iggor36gwq8904cpdvcfjc1n;

    alter table shipClass 
       drop 
       foreign key FKgkjpsgpvfaupqxr7cv9nhc9ai;

    alter table shipClass 
       drop 
       foreign key FKovqcf68xgq4mm2n32sdoburq6;

    alter table unlockedResearch 
       drop 
       foreign key FKc4x693khs2f17y0jjfb625o51;

    alter table unlockedResearch 
       drop 
       foreign key FKigikopnlfckk76o2yo3utm5s9;

    alter table user 
       drop 
       foreign key FKd0120p7tkvssh9r8hldenpw1w;

    drop table if exists alliance;

    drop table if exists building;

    drop table if exists construction;

    drop table if exists fleet;

    drop table if exists fleetcomposition;

    drop table if exists hull;

    drop table if exists job;

    drop table if exists module;

    drop table if exists moduleComposition;

    drop table if exists move;

    drop table if exists planet;

    drop table if exists research;

    drop table if exists resourceDeposit;

    drop table if exists resources;

    drop table if exists shipClass;

    drop table if exists starSystem;

    drop table if exists tick;

    drop table if exists unlockedResearch;

    drop table if exists user;
