"use client";

import { Shield, Clock, Award, Wrench } from "lucide-react";
import { useEffect, useRef, useState } from "react";

const features = [
  {
    icon: Shield,
    title: "Garantía Total",
    description:
      "Todos nuestros vehículos en venta cuentan con garantía mecánica. Alquileres con seguro incluido.",
  },
  {
    icon: Clock,
    title: "Atención 24/7",
    description:
      "Servicio de asistencia en ruta las 24 horas para todos nuestros clientes.",
  },
  {
    icon: Award,
    title: "+15 Años de Experiencia",
    description:
      "Más de 10.000 operaciones exitosas nos respaldan en el mercado automotriz argentino.",
  },
  {
    icon: Wrench,
    title: "Service Certificado",
    description:
      "Cada unidad pasa por un riguroso proceso de inspección de 150 puntos antes de la entrega.",
  },
];

const stats = [
  { value: 10000, label: "Operaciones", suffix: "+" },
  { value: 3, label: "Sucursales", suffix: "" },
  { value: 15, label: "Años", suffix: "+" },
  { value: 98, label: "Satisfacción", suffix: "%" },
];

function AnimatedCounter({ value, suffix }: { value: number; suffix: string }) {
  const [count, setCount] = useState(0);
  const [isVisible, setIsVisible] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsVisible(true);
        }
      },
      { threshold: 0.5 }
    );

    if (ref.current) {
      observer.observe(ref.current);
    }

    return () => observer.disconnect();
  }, []);

  useEffect(() => {
    if (!isVisible) return;

    const duration = 2000;
    const steps = 60;
    const increment = value / steps;
    let current = 0;

    const timer = setInterval(() => {
      current += increment;
      if (current >= value) {
        setCount(value);
        clearInterval(timer);
      } else {
        setCount(Math.floor(current));
      }
    }, duration / steps);

    return () => clearInterval(timer);
  }, [isVisible, value]);

  return (
    <div ref={ref} className="text-4xl sm:text-5xl font-black text-foreground">
      {count.toLocaleString()}{suffix}
    </div>
  );
}

export function About() {
  const [isVisible, setIsVisible] = useState(false);
  const sectionRef = useRef<HTMLElement>(null);

  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsVisible(true);
        }
      },
      { threshold: 0.2 }
    );

    if (sectionRef.current) {
      observer.observe(sectionRef.current);
    }

    return () => observer.disconnect();
  }, []);

  return (
    <section ref={sectionRef} id="nosotros" className="py-24 bg-background">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid lg:grid-cols-2 gap-16 items-start">
          {/* Left Column - Text */}
          <div
            className={`transition-all duration-700 ${
              isVisible ? "opacity-100 translate-x-0" : "opacity-0 -translate-x-12"
            }`}
          >
            <span className="text-primary text-sm font-semibold tracking-widest uppercase">
              Sobre Nosotros
            </span>
            <h2 className="text-4xl sm:text-5xl font-black mt-4 mb-6">
              <span className="text-foreground">INGENIERÍA</span>
              <br />
              <span className="text-foreground">DE MOVIMIENTO</span>
            </h2>
            <div className="space-y-4 text-muted-foreground">
              <p>
                En <strong className="text-foreground">Alsina Automóviles</strong> no
                vendemos autos, creamos experiencias de movilidad. Somos una
                agencia multimarca con presencia en la región desde 2009.
              </p>
              <p>
                Nuestra misión es conectar a cada persona con el vehículo que
                transforme su forma de moverse. Ya sea que busques comprar tu
                próximo auto o necesites un alquiler para un viaje especial,
                tenemos la solución perfecta.
              </p>
              <p>
                Trabajamos con las mejores marcas del mercado y cada unidad es
                rigurosamente inspeccionada para garantizar la máxima calidad y
                seguridad.
              </p>
            </div>
          </div>

          {/* Right Column - Features */}
          <div className="grid sm:grid-cols-2 gap-6">
            {features.map((feature, index) => (
              <div
                key={feature.title}
                className={`p-6 border border-border rounded-xl hover:border-primary/50 hover:bg-card/50 transition-all duration-300 group cursor-default ${
                  isVisible ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"
                }`}
                style={{ transitionDelay: `${index * 100 + 200}ms` }}
              >
                <feature.icon className="w-8 h-8 text-primary mb-4 transition-transform duration-300 group-hover:scale-110" />
                <h3 className="text-foreground font-semibold mb-2">
                  {feature.title}
                </h3>
                <p className="text-muted-foreground text-sm">
                  {feature.description}
                </p>
              </div>
            ))}
          </div>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-8 mt-20 pt-12 border-t border-border">
          {stats.map((stat, index) => (
            <div
              key={stat.label}
              className={`text-center transition-all duration-700 ${
                isVisible ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"
              }`}
              style={{ transitionDelay: `${index * 100 + 600}ms` }}
            >
              <AnimatedCounter value={stat.value} suffix={stat.suffix} />
              <div className="text-muted-foreground text-sm mt-1">
                {stat.label}
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
