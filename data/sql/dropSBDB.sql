
    alter table alignedFitting 
       drop 
       foreign key FKt6aos80sh8332mepbkuwmo98i;

    alter table alignedFitting 
       drop 
       foreign key FKgdp5e1ylgswr29e2d5b7uhib;

    alter table armor 
       drop 
       foreign key FK10dhr7h3pkps3d7u22q2pwpgc;

    alter table armor 
       drop 
       foreign key FKrb3h67mjdni459t4j1y8b7sw5;

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

    alter table electronicWarfare 
       drop 
       foreign key FKccj76id0r5pq3p7f4viriwdqf;

    alter table electronicWarfare 
       drop 
       foreign key FKhr2adrrpeb3vshv11ajrgnkd7;

    alter table fleet 
       drop 
       foreign key FK5yy9whqh6562iaxuym0wrkjeq;

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
       foreign key FKsevbhc9015r9wmqvojq1dbsen;

    alter table job 
       drop 
       foreign key FK4ewa76co5drr08nptgdmax8d6;

    alter table job 
       drop 
       foreign key FK3urqlpl2jmbxlfk4q88i9i5tb;

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

    alter table propulsion 
       drop 
       foreign key FKqjsvyhjc6w21niim4aeptpm85;

    alter table propulsion 
       drop 
       foreign key FK7rr2gvpcbjjhl9tuxe6c50v5q;

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
       foreign key FKouxjssb18x4jeutl5r1l0byeu;

    alter table shipClass 
       drop 
       foreign key FK5iggor36gwq8904cpdvcfjc1n;

    alter table shipClass 
       drop 
       foreign key FKfbii11hday9qcjpmi2i1k2611;

    alter table shipClass 
       drop 
       foreign key FKgkjpsgpvfaupqxr7cv9nhc9ai;

    alter table shipClass 
       drop 
       foreign key FKovqcf68xgq4mm2n32sdoburq6;

    alter table shipClass 
       drop 
       foreign key FKdd7voavc2cml9rodxm6vnlaqq;

    alter table shipClass 
       drop 
       foreign key FKsa1b1j6ur2emh3jv7s0ft3nru;

    alter table sidewall 
       drop 
       foreign key FKlo0i3byallqh89wd535yrbs3l;

    alter table sidewall 
       drop 
       foreign key FK693a9gix6ifpkiop612tghdy0;

    alter table unlockedResearch 
       drop 
       foreign key FKc4x693khs2f17y0jjfb625o51;

    alter table unlockedResearch 
       drop 
       foreign key FKigikopnlfckk76o2yo3utm5s9;

    alter table user 
       drop 
       foreign key FKd0120p7tkvssh9r8hldenpw1w;

    alter table weapon 
       drop 
       foreign key FK1rsb3ampiw8yjy8ngrget6ay;

    alter table weapon 
       drop 
       foreign key FKo22n18dgjpraqosj7nkamrnvb;

    drop table if exists alignedFitting;

    drop table if exists alliance;

    drop table if exists armor;

    drop table if exists building;

    drop table if exists construction;

    drop table if exists electronicWarfare;

    drop table if exists fleet;

    drop table if exists fleetcomposition;

    drop table if exists hull;

    drop table if exists job;

    drop table if exists move;

    drop table if exists planet;

    drop table if exists propulsion;

    drop table if exists research;

    drop table if exists resourceDeposit;

    drop table if exists resources;

    drop table if exists shipClass;

    drop table if exists sidewall;

    drop table if exists starSystem;

    drop table if exists tick;

    drop table if exists unlockedResearch;

    drop table if exists user;

    drop table if exists weapon;
