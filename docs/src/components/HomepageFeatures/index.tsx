import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  Svg: React.ComponentType<React.ComponentProps<'svg'>>;
  description: JSX.Element;
};

const FeatureList: FeatureItem[] = [
  {
    title: 'Easy to Use',
    Svg: require('@site/static/img/undraw_setup_wizard_re_nday.svg').default,
    description: (
      <>
        Almost out-of-the-box, directly use after setting up necessary configurations.
      </>
    ),
  },
  {
    title: 'Multiple Mod Loaders',
    Svg: require('@site/static/img/undraw_abstract_re_l9xy.svg').default,
    description: (
      <>
        Support Fabric and NeoForge currently, will add Spigot support in the near future.
      </>
    ),
  },
  {
    title: 'Multiple AI Platforms',
    Svg: require('@site/static/img/undraw_chat_bot_re_e2gj.svg').default,
    description: (
      <>
        Support Cloudflare and OpenAI, also OpenAI-compatible platforms that use the same request and response format as OpenAI's.
      </>
    ),
  },
];

function Feature({title, Svg, description}: FeatureItem) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): JSX.Element {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
